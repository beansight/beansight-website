import urllib
import urllib2
import getpass
import os
import imp
import tempfile
import time
import mmap
import stat
import signal
import getopt
import zipfile
from play.utils import *

# ~~~~~~~~

url = 'https://www.playapps.net'

#

MODULE = 'playapps'
COMMANDS = ['playapps:deploy']
HELP = {
    'playapps:deploy': "Deploy to your Playapps slot",
}

def load_module(name):
    base = os.path.normpath(os.path.dirname(os.path.realpath(sys.argv[0])))
    mod_desc = imp.find_module(name, [os.path.join(base, 'framework/pym')])
    return imp.load_module(name, mod_desc[0], mod_desc[1], mod_desc[2])

json = load_module('simplejson')

def execute(**kargs):
    global url
    pLay_command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")
    application_path = app.path

    try:
        optlist, args = getopt.getopt(args, '', ['url='])
        for o, a in optlist:
            if o in ('--url'):
                url = a
    except getopt.GetoptError, err:
        print "~ %s" % str(err)
        print "~ Invalid options (Use --url to specify an alternate Manager)"
        print "~ "
        sys.exit(-1)

    class UploadBuffer(object):

        width = 55

        def __init__(self, mmapped_file_as_string):
            self.mmapped_file_as_string = mmapped_file_as_string
            self.total = len(self.mmapped_file_as_string)
            self.sent = 0
            self.proc = lambda a, b: a / (b * 0.01)
            self.finished = False

        def __len__(self):
            return len(self.mmapped_file_as_string)

        def read(self, length):
            self.sent = self.sent + length
            self.progress()
            return self.mmapped_file_as_string.read(length)

        def progress(self):
            if self.sent < self.total:
                done = self.proc(self.sent, self.total)
            else:
                done = 100
            bar = self.bar(done)
            sys.stderr.write('~ [%s] \r' % (bar))

        def bar(self, done):
            span = self.width * done * 0.01
            offset = len(str(int(done))) - .99
            result = ('%d%%' % (done,)).center(self.width)
            return result.replace(' ', '-', int(span - offset))

    def fetch(path, data=None):
        request = urllib2.Request('%s%s' % (url, path), data)
        request.add_header('email', email)
        request.add_header('password', password)
        request.add_header('Accept', 'application/json')
        return urllib2.urlopen(request)

    def send(path, fd):
        upload_buf = os.path.join(tempfile.gettempdir(), '%s.zip.upload' % os.path.basename(application_path))
        if os.path.exists(upload_buf):
            os.remove(upload_buf)
        bd = 'AJNXKSNCSKNCXSKxJXUjgfjhKGHJHVhvhjbhBKhbkjkjnlkJkjnjlknLKJNJKN'
        buf = open(upload_buf, 'wb')
        buf.write('--%s\r\n' % bd)
        buf.write('Content-Disposition: form-data; name="archive"; filename="archive.zip"\r\n')
        buf.write('Content-Type: application/octet-stream\r\n')
        fd.seek(0)
        buf.write('\r\n' + fd.read() + '\r\n')
        buf.write('--%s--\r\n\r\n' % bd)
        buf.close()
        buf = open(upload_buf, 'rb')
        mmapped_file_as_string = mmap.mmap(buf.fileno(), 0, access=mmap.ACCESS_READ)
        if sys.version_info < (2, 6):
             data = mmapped_file_as_string
             print '~ Sorry, no upload progress bar with python2.5 (try python2.6) -> Uploading %sMB anyway...' % (os.path.getsize(archive_path)/1024/1024)
        else:
             data = UploadBuffer(mmapped_file_as_string)
        request = urllib2.Request('%s%s' % (url, path), data)
        request.add_header('Content-Type', 'multipart/form-data; boundary=%s' % bd)
        request.add_header('email', email)
        request.add_header('password', password)
        request.add_header('Accept', 'application/json')
        return urllib2.urlopen(request)

    try:

        archive_path = os.path.join(tempfile.gettempdir(), '%s.zip' % os.path.basename(application_path))
        if os.path.basename(application_path) == 'main':
            td_path = os.path.dirname(application_path)
        else:
            td_path = application_path
        print "~ Creating archive from %s to %s ..." % (td_path, os.path.normpath(archive_path))
        if os.path.exists(archive_path):
            os.remove(archive_path)
        zip = zipfile.ZipFile(archive_path, 'w', zipfile.ZIP_STORED)
        data_dir = os.path.join(application_path, 'data')
        tmp_dir = os.path.join(application_path, 'tmp')
        logs_dir = os.path.join(application_path, 'logs')
        db_dir = os.path.join(application_path, 'db')
        for (dirpath, dirnames, filenames) in os.walk(td_path):
            if dirpath.startswith(data_dir) or dirpath.startswith(tmp_dir) or dirpath.startswith(logs_dir) or dirpath.startswith(db_dir):
                continue
            if dirpath.find('/.') > -1:
                continue
            for file in filenames:
                if file.find('~') > -1 or file.startswith('.'):
                    continue
                zip.write(os.path.join(dirpath, file), os.path.join(dirpath[len(td_path):], file))
        zip.close()
        print '~ Done (%sMB)' % (os.path.getsize(archive_path)/1024/1024)
        print '~ '
        
        email = raw_input("~ What is your email? ")
        password = getpass.getpass("~ What is your password? ")
        print '~'

        try:
            slots = json.loads(fetch('/%s' % email).read())
            if not 'slots' in slots:
                print '~ Cannot connect'
                print '~'
                sys.exit(-1)
        except urllib2.HTTPError, e:
            print '~ Cannot connect (%s)' % e.code
            print '~'
            sys.exit(-1)

        print '~ Connected, choose your application:'
        i = 1
        for s in slots['slots']:
            print '~ \t%s. %s' % (i, s['name'])
            i = i + 1

        try:
            slot = int(raw_input('~ ? '))
            slot = slots['slots'][slot-1]['name']
        except Exception, err:
            print '~ Oops (%s)' % err
            print '~'
            sys.exit(-1)

        manager = '%s/%s/%s' % (url, email, slot)

        try:
            print '~'
            print '~ Checking %s state...' % slot
            status = json.loads(fetch('/%s/%s/overview/status ' % (email, slot)).read())
            httpMode = status['operation']['httpMode']
            app = status['operation']['application']
            privateHost = status['privateHost']
            runningTask = status['runningTask']
        except urllib2.HTTPError, e:
            print '~ Cannot access application (%s)' % e.code
            print '~'
            sys.exit(-1)

        if runningTask:
            print '~'
            print '~ There is another task already running. Wait for all tasks completions'
            print '~ Check the web console at %s' % manager
            print '~'
            sys.exit(-1)

        if not httpMode == 'maintenance' or not app == 'HALTED':
            print '~'
            print '~ We need to stop your application and set the HTTP server in maintenance before before deploying'
            sure = raw_input('~ Are you sure [y/N]? ')
            if sure == 'y':
                print '~'
                print '~ Setting the slot in maintenance mode...'
                fetch('/%s/%s/maintenance' % (email, slot), urllib.urlencode({}))
                status = json.loads(fetch('/%s/%s/overview/status ' % (email, slot)).read())
                httpMode = status['operation']['httpMode']
                app = status['operation']['application']
                if not httpMode == 'maintenance' or not app == 'HALTED':
                    print '~ Failed! Check the web console at %s' % manager
                    print '~'
                    sys.exit(-1)
                else:
                    print '~ Ok'
            else:
                print '~'
                sys.exit(-1)

        else:
            print '~ The application is in maintenance mode, continuing'

        try:
            print '~'
            print '~ Verifying backups...'
            status = json.loads(fetch('/%s/%s/backup/status ' % (email, slot)).read())
            dates = []
            for sn in status['snapshots']:
                dates.append(sn['date'])
            dates.sort()
            dates.reverse()
            message = "You don't have any backup."
            if len(dates) > 0:
                message = "Your last backup has been created on %s" % time.ctime(dates[0])

            print "~ %s" % message
            sure = raw_input('~ Do you want to create a backup now [Y/n]? ')

            if not sure == 'n':
                print '~'
                print '~ Creating backup...'
                status = json.loads(fetch('/%s/%s/backup/snapshot ' % (email, slot), urllib.urlencode({})).read())
                if not status['result'] == 'OK':
                    print '~ Failed! Check the web console at %s' % manager
                    print '~'
                    sys.exit(-1)
                else:
                    print '~ Ok'

        except urllib2.HTTPError, e:
            print '~ Cannot access application (%s)' % e.code
            print '~'
            sys.exit(-1)

        print '~ '
        print '~ Uploading archive...'
        status = json.loads(send('/%s/%s/deploy/archive ' % (email, slot), open(archive_path, "rb")).read())

        print
        print '~'
        print '~ Installing....'

        done = 0
        installed = False
        while True:
            span = 55 * done * 0.01
            offset = len(str(int(done))) - .99
            result = ('%d%%' % (done,)).center(55)
            bar = result.replace(' ', '-', int(span - offset))
            sys.stderr.write('~ [%s] \r' % (bar))
            if installed:
                break
            done = int(fetch('/tasks/%s' % status['task']).read().strip())
            if done == -1:
                done = 100
                installed = True
            time.sleep(3)

        print
        print '~'

        sure = raw_input('~ Do you want to start the application now [Y/n]? ')
        if sure == 'n':
            print '~'
            print '~ You can access the web console at %s' % manager
            sys.exit(0)

        print '~'
        print '~ Starting the application...'
        fetch('/%s/%s/application/start' % (email, slot), urllib.urlencode({}))

        while True:
            time.sleep(2)
            status = json.loads(fetch('/%s/%s/overview/status ' % (email, slot)).read())
            app = status['operation']['application']
            if app == 'RUNNING':
                print '~ Started'
                print '~'
                print '~ You can use your private access to check the application at http://%s' % privateHost
                print '~ Then open the HTTP server using the web console at %s' % manager
                print '~'
                sys.exit(0)
            if app == 'HALTED':
                errors = fetch('/%s/%s/logs/error' % (email, slot)).read()
                print '~ Failed,'
                print '~ last error is:'
                print ''
                print errors
                print ''
                print '~ Check the web console at %s' % manager
                print '~'
                sys.exit(-1)

    except Exception, err:
        print err
        print "~ %s" % str(err)
        print "~ "
        sys.exit(-1)

def after(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")

    if command == 'new':
        try:
            print "~ Adding playapps.net default configuration to application.conf"
            print "~"

            ac = open('%s/conf/application.conf' % app.path, 'r')
            conf = ac.read()
            conf = """# playapps.net configuration
# ~~~~~
%playapps.application.mode=prod
%playapps.application.log=INFO
%playapps.db=mysql:play:play@play
%playapps.jpa.ddl=update

""" + conf
            ac = open('%s/conf/application.conf' % app.path, 'w')
            ac.write(conf)

        except Exception, err:
            print "~ %s" % str(err)
            print "~ "
            sys.exit(-1)
