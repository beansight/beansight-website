<div id="userOptions">
        #{isNotConnected}
    <span id="signupLink"><a href="" onclick="return false">Sign up</a></span>
    <span id="loginLink"><a href="" onclick="return false">Log in</a></span>
        #{/isNotConnected}
        #{isConnected}
            ${controllers.CurrentUser.getCurrentUserName()}
            <a href="@{Application.settings()}">Settings</a>
            <a href="@{Secure.logout()}">Logout</a>
        #{/isConnected}
</div>

<script type="text/javascript">
    $("#signupLink").click(function() {
        $("#loginBox").hide();
        var box = $("#signupBox");
        if (box.is(":visible")) {
            box.fadeOut();
        } else {
            box.fadeIn();
        }
    });

    $("#loginLink").click(function() {
        $("#signupBox").hide();
        var box = $("#loginBox");
        if (box.is(":visible")) {
            box.fadeOut();
        } else {
            box.fadeIn();
        }
    });
</script>

#{if !controllers.Secure.Security.isConnected()}
    <div id="signupBox" class="floatingBox" style="display:none;">
    <h2>Sign Up</h2>
    #{form @Register.registerNew()}
        <p id="email-field">
            <label for="email">Email</label>
            <input type="email" name="email" id="email" value="&{flash.email}" />
        </p>
        <p id="username-field">
            <label for="username">User name</label>
            <input type="text" name="username" id="username" value="&{flash.username}" />
        </p>
        <p id="password-field">
            <label for="password">Password</label>
            <input type="password" name="password" id="password" value="" />
        </p>
        <p id="signin-field">
        <input type="submit" id="signin" value="&{'signup'}" />
        </p>
    #{/form}
    </div>

    <div id="loginBox" class="floatingBox" style="display:none;">
    <h2>Log in</h2>
       #{form @Secure.authenticate()}
                <div>
	                <label for="username">Email</label> *{ Secure module works using "username", but for us, username is the email }*
	                <input type="email" name="username" id="username" value="${flash.username}" />
	                <label for="password">Password</label>
	                <input type="password" name="password" id="password" value="" />
                </div>
                <div>
	                <input type="checkbox" name="remember" id="remember" value="true" ${flash.remember ? 'checked="true"' : ''} />
	                <label for="remember">remember?</label>
	                <input type="submit" id="signin" value="&{'login'}" />
                </div>
        #{/form}
    </div>
#{/if}