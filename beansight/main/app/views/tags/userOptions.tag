#{isNotConnected}
<div id="loginbox">
    <p class="cuf-loginwidth">&{'loginlink'}</p>
    <ul class="listlogs">
        <li class="log-b" id="lilogb"><a href="#" class="current" id="log-bean">Beansight</a>
            <div class="item-loginbox" id="boxlog-bean" style="display:none;">
                #{form @Secure.authenticate()}
                    <label for="username">&{'email'}</label>
                    <div class="inputlogin">
                        <input type="email" name="username" value="${flash.username}" id="pseudologin" class="clearinput"/>
                    </div>
                    <label for="password">&{'password'}</label>
                    <div class="inputlogin">
                        <input type="password" name="password" value="" id="passlogin" class="clearinput"/>
                    </div>
                    <div class="avatarlogin">
                        <img src="images/content/sample-26x26.jpg" alt=""/>
                    </div>
                    
                    <input style="display:none;" type="checkbox" name="remember" id="remember" value="true" checked="true" />
                    
                    <div class="inputsubmit">
                        <button><span class="backbutton"></span><span class="txtbutton cuf-connect">&{'loginbutton'}</span></button>
                        <a href="@{Security.forgotPasswordAskEmail()}" class="forgotten">&{'userOptions.forgotPassword'}</a>
                    </div>
                #{/form}
            </div>
        </li>
        <li class="log-t"><a href="@{TwitterOAuth.loginWithTwitter}" id="log-twit">Twitter</a></li>
        <li class="log-f"><a href="@{FaceBookOAuth.authenticate}" id="log-fb">Facebook</a></li>
    </ul>
</div>
#{/isNotConnected}


#{isConnected}
<div class="signedInUserOption">
<div class="signbtn">
    <a href="@{Secure.logout()}" class="cuf-grb">&{'logoutlink'}</a>
</div>
<div class="welcome">
    <p>&{'welcomeusername'} <a href="@{Application.profile()}" class="pseudo-link">${controllers.CurrentUser.getCurrentUserName()}</a></p>
    <p><a href="@{Application.settings()}" class="setting-link">&{'settingslink'}</a></p>
</div>
</div>
#{/isConnected}

