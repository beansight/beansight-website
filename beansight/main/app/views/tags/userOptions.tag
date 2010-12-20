<div id="userOptions">
#{isNotConnected}
    <span id="signupLink"><a href="@{Register.register}">&{'signuplink'}</a></span>
    <span id="loginLink"><a href="" onclick="return false">&{'loginlink'}</a></span>
    <span id="loginLink"><a href="@{TwitterOAuth.loginWithTwitter}"><img src="/public/images/twitter-login-button.png" height="20px" style="margin-top: 5px;"/></a></span>
    <span id="loginLink"><a href="@{FaceBookOAuth.authenticate}"><img src="/public/images/facebook-login-button.png" height="20px"/></a></span>
#{/isNotConnected}
#{isConnected}
    ${controllers.CurrentUser.getCurrentUserName()}
    <a href="@{Application.settings()}">&{'settingslink'}</a>
    <a href="@{Secure.logout()}">&{'logoutlink'}</a>
#{/isConnected}
</div>

#{isConnected}
    <div id="loginBox" class="floatingBox" style="display:none;">
    <h2>&{'logintitle'}</h2>
       #{form @Secure.authenticate()}
                <div>
	                <label for="username">&{'email'}</label> *{ Secure module works using "username", but for us, username is the email }*
	                <input type="email" name="username" id="username" value="${flash.username}" />
	                <label for="password">&{'password'}</label>
	                <input type="password" name="password" id="password" value="" />
                </div>
                <div>
	                <input type="checkbox" name="remember" id="remember" value="true" ${flash.remember ? 'checked="true"' : ''} />
	                <label for="remember">&{'remember'}</label>
	                <input type="submit" id="signin" value="&{'loginbutton'}" />
                </div>
        #{/form}
    </div>
#{/isConnected}