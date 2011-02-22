#{isNotConnected}
<div id="loginbox" class="loginbox">
    <p class="cuf-loginwidth">&{'loginlink'}</p>
    <ul class="listlogs">
        <li class="log-b" id="lilogb">
            <a href="#" class="current logbutton" id="log-bean">Beansight</a>
            <div class="item-loginbox" id="boxlog-bean" style="display:none;">
                #{form @Secure.authenticate(), class:'boxlogContent', id:'logBeanForm'}
                    <label for="username">&{'email'}</label>
                    <div class="inputlogin">
                        <input type="email" name="username" value="${flash.username}" id="pseudologin" class="clearinput"/>
                    </div>
                    <label for="password">&{'password'}</label>
                    <div class="inputlogin">
                        <input type="password" name="password" value="" id="passlogin" class="clearinput"/>
                    </div>
                    <div class="avatarlogin">
                        <img id="avatarlogin" src="/public/images/avatar/empty-small.jpg" />
                    </div>
                    <input style="display:none;" type="checkbox" name="remember" id="remember" value="true" checked="true" />
                    <div class="inputsubmit">
                        <button class="goButton"><span class="backbutton"></span><span class="txtbutton cuf-connect">&{'loginbutton'}</span></button>
                        <a href="@{Security.forgotPasswordAskEmail()}" class="forgotten">&{'userOptions.forgotPassword'}</a>
                    </div>
                #{/form}
            </div>
        </li>
        <li class="log-t">
            <a href="#" id="log-twit" class="logbutton">Twitter</a>
            <div class="item-loginbox" id="boxlog-twit" style="display:none;">
                <div class="boxlogContent">
                <label>&{'userOptions.makesureHavingPromocode'}</label>
                <div class="inputsubmit">
                    <button class="goButton" id="logButton-twit"><span class="backbutton"></span><span class="txtbutton cuf-connect">&{'userOptions.confirmHavingPromocode'}</span></button>
                    <script type="text/javascript">
                        $("#logButton-twit").click(function() {
                             window.location.href = "@{TwitterOAuth.authenticate}";
                        })
                    </script>
                </div>
                </div>
            </div>
        </li>
        <li class="log-f">
            <a href="" id="log-fb" class="logbutton">Facebook</a>
            <div class="item-loginbox" id="boxlog-fb" style="display:none;">
                <div class="boxlogContent">
                <label>&{'userOptions.makesureHavingPromocode'}</label>
                <div class="inputsubmit">
                    <button class="goButton" id="logButton-fb"><span class="backbutton"></span><span class="txtbutton cuf-connect">&{'userOptions.confirmHavingPromocode'}</span></button>
                    <script type="text/javascript">
                        $("#logButton-fb").click(function() {
                             window.location.href = "@{FaceBookOAuth.authenticate}";
                        })
                    </script>
                </div>
                </div>
            </div>
        </li>
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

