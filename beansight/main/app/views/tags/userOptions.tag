#{isNotConnected}
<div class="signbtn">
    <a href="@{Register.register}" class="cuf-grb">&{'signuplink'}</a>
</div>
<div class="logbtn" id="zonelogin">
    <a href="#" class="cuf-grb" id="btnlogin">&{'loginlink'}</a>
    <div id="boxlogin" style="display:none;">
        <div class="listlogin">
            <ul>
                <li class="log-b"><a href="#" class="current" id="log-bean">Beansight</a></li>
*{                
                <li class="log-t"><a href="#" id="log-twit">Twitter</a></li>
                <li class="log-f"><a href="#" id="log-fb">Facebook</a></li>
}*                
            </ul>
        </div>

        <div class="list-logins">
            <div class="item-loginbox" id="boxlog-bean">
                #{form @Secure.authenticate()}
                    <label for="username">&{'email'}</label> *{ Secure module works using "username", but for us, username is the email }*
                    <div class="inputlogin">
                        <input type="email" name="username" value="${flash.username}" id="pseudologin"/>
                    </div>
                    <label for="password">&{'password'}</label>
                    <div class="inputlogin">
                        <input type="password" name="password" value="" id="passlogin"/>
                    </div>
                    
                    <input type="checkbox" name="remember" id="remember" value="true" ${flash.remember ? 'checked="true"' : ''} />
                    <label for="remember">&{'remember'}</label>
                    
                    <div class="inputsubmit">
                        <button><span class="backbutton"></span><span class="txtbutton cuf-connect">&{'loginbutton'}</span></button>
                    </div>
                #{/form}
            </div>
*{
            <div class="item-loginbox nojavascript" id="boxlog-twit">
                <a href="@{TwitterOAuth.loginWithTwitter}"><img src="/public/images/twitter-login-button.png" height="20px" style="margin-top: 5px;"/></a>
            </div>

            <div class="item-loginbox nojavascript" id="boxlog-fb">
                <a href="@{FaceBookOAuth.authenticate}"><img src="/public/images/facebook-login-button.png" height="20px"/></a>
            </div>
}*
        </div>
    </div>
</div>
#{/isNotConnected}


#{isConnected}
<div class="signbtn">
    <a href="@{Secure.logout()}" class="cuf-grb">&{'logoutlink'}</a>
</div>
<div class="welcome">
    <p>&{'welcomeusername'} <a href="@{Application.profile()}" class="pseudo-link">${controllers.CurrentUser.getCurrentUserName()}</a></p>
    <p><a href="@{Application.settings()}" class="setting-link">&{'settingslink'}</a></p>
</div>
#{/isConnected}

