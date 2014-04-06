#{isNotConnected}
<a href="#" class="logbtn" id="log-bean">&{'logintitle'}</a>
<div id="loginbox" class="loginbox" style="display:none;"> 
    #{form @Register.beansightAuthenticate(), class:'boxlogContent', id:'logBeanForm'}
        <label for="username">&{'email'}</label> 
        <div class="inputlogin"> 
            <input type="email" name="username" value="" id="pseudologin" class="clearinput" placeholder="&{'email'}"/> 
        </div> 
        <label for="password">&{'password'}</label> 
        <div class="inputlogin"> 
            <input type="password" name="password" value="" id="passlogin" class="clearinput" placeholder="&{'password'}"/> 
        </div> 
        <input style="display:none;" type="checkbox" name="remember" id="remember" value="true" checked="true" /> 
        <input type="hidden" name="url" id="url" value="${request.url}" /> 
        <div class="inputsubmit"> 
            <a href="@{Security.forgotPasswordAskEmail()}" class="forgotten">&{'userOptions.forgotPassword'}</a> 
            <button class="goButton"><span class="backbutton"></span><span class="txtbutton">&{'loginbutton'}</span></button> 
        </div> 
    #{/form}
    <a href="@{Register.fbAuthenticate(request.url)}" class="btnlogwith logwithfb social_buttons sb_24 sb_facebook"><span>&{'userOptions.loginwithfacebook'}</span></a>
    <a href="@{Register.twitAuthenticate(request.url)}" class="btnlogwith social_buttons sb_24 sb_twitter"><span>&{'userOptions.loginwithtwitter'}</span></a>
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

