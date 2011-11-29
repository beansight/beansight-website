echo "Create a folder, create .html files with HTML code (that you get by showing the sources of the pages). Then enter the name of the folder:"
read -e FOLDER
svn export ../../Website/trunk/beansight/main/public $FOLDER/public
find ./ -type f -name '*.html' -exec sed -i 's/\/public/public/g' {} \;
find ./ -type f -name '*.css' -exec sed -i 's/\/public/../g' {} \;
