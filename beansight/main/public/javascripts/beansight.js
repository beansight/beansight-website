// Size of an insight
var MAX_CHARACTERS_INSIGHT = 120;

//////////////////////
// Cufon Settings
//////////////////////
Cufon.set('fontFamily', 'got-rnd-book').replace('.cuf-grb', {hover: true});
Cufon.set('fontFamily', 'got-rnd-med').replace('.cuf-grm', {hover: true});
Cufon.set('fontFamily', 'got-rnd-bold').replace('.cuf-grs', {hover: true});
Cufon.set('fontFamily', 'got-rnd-med').replace('.shadowtwit', {textShadow: '#54d5f6 1px 1px'}, {hover: true});
Cufon.set('fontFamily', 'got-rnd-med').replace('.shadowfb', {textShadow: '#223974 1px 1px'}, {hover: true});
//Cufon.set('fontFamily', 'myriad pro').replace('#sidebar-signup h4', {textShadow: '#ee7c63 1px 1px'}, {fontWeight:'400'});
Cufon.set('fontFamily', 'got-rnd-book').replace('.cuf-connect', {textShadow: '#186877 0px -1px'}, {hover: true});
Cufon.set('fontFamily', 'got-rnd-book').replace('.cuf-loginwidth', {hover: true});
Cufon.set('fontFamily', 'got-rnd-med').replace('.cuf-invitation', {textShadow: '#5e872a 0px -1px'}, {hover: true});
Cufon.set('fontFamily', 'got-rnd-book').replace('.section-title',{hover: true});
Cufon.set('fontFamily', 'got-rnd-book').replace('.pageintro',{hover: true});


//////////////////////
// Actions on insights
//////////////////////

/** Current user agree an insight */
function agree(insightUniqueId) {
    $.getJSON(agreeAction({'insightUniqueId': insightUniqueId}), onVoteSuccess);
    
    var insightContainer = $(".insight_" + insightUniqueId);
   	insightContainer.removeClass("voteDisagree").addClass("voteAgree");

   	$(".voteWidgetLarge #lastVote").html(i18n.voteAgree);
   	
   	return false;
}

/** Current user disagree an insight */
function disagree(insightUniqueId) {
    $.getJSON(disagreeAction({'insightUniqueId': insightUniqueId}), onVoteSuccess);

    var insightContainer = $(".insight_" + insightUniqueId);
   	insightContainer.addClass("voteDisagree").removeClass("voteAgree");

   	$(".voteWidgetLarge #lastVote").html(i18n.voteDisagree);
   	
    return false;
}

/** Callback after a vote is done */
function onVoteSuccess(data) {
    updateAgreeDisagreeCount(data.uniqueId, data.updatedAgreeCount, data.updatedDisagreeCount, data.voteState);
}

/** Update the counts of an insight, given new counts */
function updateAgreeDisagreeCount(uniqueId, agreeCount, disagreeCount, voteState) {
	var insightContainer = $(".insight_" + uniqueId);
    $(".agreeCount", insightContainer).text(agreeCount);
    $(".disagreeCount", insightContainer).text(disagreeCount);
    
    if(voteState == "agree") {
    	insightContainer.removeClass("voteDisagree").addClass("voteAgree");
    } else {
    	insightContainer.addClass("voteDisagree").removeClass("voteAgree");
    }
}

function toggleFollowingInsight(insightUniqueId) {
	$.getJSON(toggleFollowingInsightAction({'insightUniqueId': insightUniqueId}), onToggleFollowingInsightSuccess);
	// Change the color of the icon, before receiving any response.
	var favicon = $(".addfav", ".insight_" + insightUniqueId);
	if(favicon.hasClass("active")) {
		favicon.removeClass("active");
		$(".item-fav .insight_" + insightUniqueId).remove();
	} else {
		favicon.addClass("active");
	}
	return false;
}

/** Callback after a follow of insight is done */
function onToggleFollowingInsightSuccess(data) {
	var favicon = $(".addfav", ".insight_" + data.uniqueId);
	if(data.follow) {
		favicon.addClass("active");
		$.ajax({
	        url: getFavoriteInsightAction({'insightUniqueId':data.uniqueId}),
	        dataType: 'html',
	        success: function(data) {
	        	$(".list-fav .clear").before(data);
	        }
	    });
	} else {
		favicon.removeClass("active");
	}
}

function toggleFollowingUser(userId) {
	$.getJSON(toggleFollowingUserAction({'userId': userId}), onToggleFollowingUserSuccess);
	var favicon = $(".addfav", ".user_" + userId);
	if(favicon.hasClass("active")) {
		favicon.removeClass("active");
	} else {
		favicon.addClass("active");
	}
	return false;
}

/** Callback after a follow of user is done */
function onToggleFollowingUserSuccess(data) {
	var favicon = $(".addfav", ".user_" + data.id);
	if(data.follow) {
		favicon.addClass("active");
	} else {
		favicon.removeClass("active");
	}
	$.get(loadFollowedUsersBlockAction({'userId': data.id}), onLoadFollowedUsersSuccess);
}

function toggleFollowingTopic(topicId) {
	$.getJSON(toggleFollowingTopicAction({'topicId': topicId}), onToggleFollowingTopicSuccess);
	var favicon = $(".addfav.topic_" + topicId);
	if(favicon.hasClass("active")) {
		favicon.removeClass("active");
	} else {
		favicon.addClass("active");
	}
	return false;
}

/** Callback after a follow of topic is done */
function onToggleFollowingTopicSuccess(data) {
	var favicon = $(".addfav", ".topic_" + data.id);
	if(data.follow) {
		favicon.addClass("active");
	} else {
		favicon.removeClass("active");
	}
}

/** Callback  */
function onLoadFollowedUsersSuccess(data) {
	$(".list-avatars").replaceWith(data);
}

/** Callback after a resetInsightActivity is done*/
function onResetInsightActivitySuccess(data) {
	$('#insightActivity').fadeOut();
	$('#noInsightActivity').fadeIn();
}

/** Update the progress bar and characters count */
function updateCharacterCount() {
	try {
		var content = $("#insightContent").val();
		var size = content.length;
		var percent = size / MAX_CHARACTERS_INSIGHT * 100;
		$("#currentCaractersNumbers").html(size);
		$( "#progressbar" ).progressbar({
			value: percent
		});
		
		if( size > MAX_CHARACTERS_INSIGHT ) {
			$("#caracterNumbers").addClass("tooMuchCharacters");
		} else {
			$("#caracterNumbers").removeClass("tooMuchCharacters");
		}
	} catch(e) {}
}

/** callback for comment addition */
function onAddCommentSuccess(content) {
	$(".ajaxloader").hide();
    $("#commentList").prepend( content );
    clearForm('#addCommentForm');
}

//////////////////////
// Create Insight
//////////////////////
    /* associate the click action to tags */
    function clicktags(){
        $(".listtags a").each(function(i){
            $(this).click(function(e) {
                $(this).remove();
                registertags();
                return false;
            });
        });
    }
    clicktags();

	/* clear the new tag */
    function validetag(){
        $('#newtag').attr('id', '');
        $('#taginput').val('');
    }

	/* transform tags in a tag string to store in the tagresult */
    function registertags(){
        // reset the tagresult content.
        var tagresult = $('#tagresult');
        tagresult.val('');
		// for each tag, add it to the value field.
        $(".listtags a span").each(function(i){
        	// if first do not add a comma
            if(i > 0) {
                tagresult.val( tagresult.val() + ', ');
            }
            tagresult.val( tagresult.val() + $(this).html() );
        });
    }

	/* updates the newtag */
	function updateTags() {
		var taginput = $('#taginput');
		// if taginput has a value
		if( taginput.val() != '' ){
			// if no new tag is currently constructing, create it
		    if( ! $('#newtag').length ){
		    	
		    	var newTagString = $.trim($('#taginput').val().replace(/(;|,)/ig, ''));
		    	if(newTagString.length > 0) {
			        $('<a href="#" id="newtag">' + newTagString + '</a>').appendTo(".listtags");
			        clicktags();
		    	}
		    }
		    
		    // if a comma has been entered, then delete the newtag
		    if(taginput.val().indexOf(',')!=-1 || taginput.val().indexOf(';')!=-1 || taginput.val().indexOf(' ')==taginput.val().length ){ // if a separator is entered
		        validetag();
        	}else{ // in any other case, update the newtag
            	$('#newtag').html('<span>' + $.trim(taginput.val()) + '</span>');
        	}
	        registertags();
        } else {
        	$('#newtag').remove();
    	}
	}

	
//////////////////////
// Settings
//////////////////////
	
var imgAreaSelect;
function onOpenModalCrop(event, ui) {
    imgAreaSelect = $('#uploadedImage').imgAreaSelect({ 
        instance:       true,
        aspectRatio:    '1:1', 
        handles:        true,
        fadeSpeed:      200,
        onSelectChange: preview
    });
}

function onCloseModalCrop(event, ui) {
    imgAreaSelect.setOptions({
        remove: true,
        hide:   true
    }); 
    imgAreaSelect.update();

    imgAreaSelect = null;
}

function preview(img, selection) {
     
     if (!selection.width || !selection.height)
     return;
     
     var scaleX = 100 / selection.width;
     var scaleY = 100 / selection.height;

     $('#preview img').css({
         width: Math.round(scaleX * 300),
         height: Math.round(scaleY * $("#uploadedImage").height()),
         marginLeft: -Math.round(scaleX * selection.x1),
         marginTop: -Math.round(scaleY * selection.y1)
     });

     $('#x1').val(selection.x1);
     $('#y1').val(selection.y1);
     $('#x2').val(selection.x2);
     $('#y2').val(selection.y2);
     $('#imageW').val(300);
     $('#imageH').val($("#uploadedImage").height());
}


// Tools
function clearForm( context ) {
    $(':input', context)
     .not(':button, :submit, :reset, :hidden')
     .val('')
     .removeAttr('checked')
     .removeAttr('selected');
}

function abbreviate(str, size) {
	if (str.length <= size) {
		return str;
	}
	return str.substring(0,size) + "...";
}

function insightHideComment(commentId) {
	if(confirm('confirm ?')) {
		$.get(insightHideCommentAction({'commentId':commentId}), function(data) {
			if(data.error=="") {
				$('#insightComment_' + data.id).remove();
			} else {
				alert(data.error);
			}
		});
	}
}

function showRegisterForm(formToShow) {
	$('#signup-bean').removeClass('opened');
	$('#signup-twit').removeClass('opened');
	$('#signup-fb').removeClass('opened');
	
	$("#beansightRegisterBlock").hide();
	$("#facebookRegisterBlock").hide();
	$("#twitterRegisterBlock").hide();
	
	if (formToShow == 'beansight') {
		$("#beansightRegisterBlock").show();
		$('#signup-bean').addClass('opened');
	} else if(formToShow == 'twitter') {
		$("#twitterRegisterBlock").show();
		$('#signup-twit').addClass('opened');
	} else if(formToShow == 'facebook') {
		$("#facebookRegisterBlock").show();
		$('#signup-fb').addClass('opened');
	}
}

///////////////
// if modifying these 3 functions don't forget to also update their java counterpart in FormatHelper.java
///////////////
function replaceAtWithProfilLinks(text) {
	var reg=new RegExp("(\\W*@([\\w]+))", "g");
	return text.replace(reg, "<a href='/expert/$2'>$1</a>");
}
	
	
function linkify(inputText) {
    //URLs starting with http://, https://, or ftp://
    var replacePattern1 = /(\b(https?|ftp):\/\/[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/gim;
    var replacedText = inputText.replace(replacePattern1, '<a href="$1" target="_blank">$1</a>');

    //URLs starting with www. (without // before it, or it'd re-link the ones done above)
    var replacePattern2 = /(^|[^\/])(www\.[\S]+(\b|$))/gim;
    var replacedText = replacedText.replace(replacePattern2, '$1<a href="http://$2" target="_blank">$2</a>');

    //Change email addresses to mailto:: links
    var replacePattern3 = /(\w+@[a-zA-Z_]+?\.[a-zA-Z]{2,6})/gim;
    var replacedText = replacedText.replace(replacePattern3, '<a href="mailto:$1">$1</a>');

    return replacedText
}

function nl2br(value) {
	 return value.replace(/\n/g, "<br/>");
}

function onVotedMouseDown(object) {
	if ( $('#radioVoteVoted').is(':checked') ) {
		if ( !$('#radioVoteNotVoted').is(':checked') ) {
			return false;
		}
	}
	return true;
}

/**
 * @returns the arguments needed for getInsightsAction(); 
 */
function generateGetInsightsArguments() {
	var sortBy = $('input[name=SortByGroup]:checked').val(); 
	var cat = $('#filterCategory').val();
	var filterVote = $('input[name=VoteGroup]:checked').val(); 
	return {'from':insightsFrom, 'sortBy': sortBy,  'cat':cat, 'filterVote':filterVote, 'topic':filterTopic, 'closed':closedInsight};
}

/**
 * @returns the arguments needed for getInsightsAction(); 
 */
function generateGetUserInsightsArguments() {
	var userName = $('input[name=userName]:hidden').val(); 
	var cat = $('#userInsightsFilterCategory').val();
	var filterVote = $('input[name=userVoteGroup]:checked').val(); 
	return {'from':insightsFrom, 'userName': userName, 'cat': cat, 'filterVote': filterVote};
}

/**
 * replace the insight list with a new one
 */
function loadInsights(urlFct, paramsFct) {
	insightsFrom = 0;
	$.get( urlFct(paramsFct()), function(content) {
		$('#insightList').html(content);
		//insightsFrom += NUMBER_INSIGHTS_INSIGHTPAGE;
		postProcessContent();
	});
}

/**
 * append to the insight list more results
 */
function loadMoreInsights(urlFct, paramsFct) {
	insightsFrom = parseInt(insightsFrom) + NUMBER_INSIGHTS_INSIGHTPAGE;
	$.get( urlFct(paramsFct() ), function(content) {
		$('#insightList').append(content);
		postProcessContent();
	});
}
	
function postProcessContent() {
	$("#insightList .loginTooltip").tooltip({showURL: false});
}

/**
 * append to the insight list more results
 */
function reloadInsights(path) {
	$.get( path, function(content) {
		$('#insightList').html(content);
		postProcessContent();
	});
}

/**
 * call this function to bind a state in history.
 * Using this function prevent the history to trigger the load of the setted hash
 */
function bindCurrentState(hashStateToSave) {
	// setting bindingTime as true insure that no event will be triggered when setting the hash
	bindingTime = true;
	$.History.setHash(hashStateToSave);
}


function refreshFilters(str) {
	var sortBy = gup(str, "sortBy"); // incoming, trending, updated
	var voteFilter = gup(str, "filterVote"); // all, voted, notVoted
	
	$("input[type=radio]").val([sortBy, voteFilter]);
	
	var category = gup(str, "cat");
	$("#filterCategory option[value='" + category + "']").attr('selected', 'selected');
	
	// call this to have the jqueryui refreshed and see the change
	$(':radio').button('refresh');
	$('#filterCategory').button('refresh');
}

function refreshUserInsightsFilters(str) {
	var filterVote = gup(str, "filterVote");
	$("input[type=radio]").val([sortBy, voteFilter]);
	
	var category = gup(str, "cat");
	$("#filterCategory option[value='" + category + "']").attr('selected', 'selected');
	
	// call this to have the jqueryui refreshed and see the change
	$('#filterCategory').button('refresh');
}

/**
 * Extract parameters from the string
 * @param str : something?test=1&toto=2&value=titi
 * @param name : for example using the previous string : test, toto or value
 * @returns
 */
function gup( str, name ) {
	name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
	var regexS = "[\\?&]"+name+"=([^&#]*)";
	var regex = new RegExp( regexS );
	var results = regex.exec( str );
	if( results == null )
	  return "";
	else
	  return results[1];
}

/**
 * Display a message at the top of the screen to go to the mobile version of the website
 */
function displayMobileMessage() {
	$('#gotoMobile').show();
}

// Execute scripts after the document creation
$(document).ready(function() {
	
    // delete nojavascript class for those who have javascript
    $('.nojavascript').removeClass('nojavascript');
	
    // Overload jQuery error
    /** Called when an AJAX request returns an error */
    $("#error").ajaxError(function(event, request, settings){
        $(this).text('Sorry, an error occured during last action.');
    });
    
    //select custom
    $(".item-select select").selectbox();
	
    // every .uiButton is transformed in a button with jQuery UI
    $('.uiButton').button();

    //////////////////////
    // Footer
    //////////////////////
	$.ajax({
        url: 'http://api.twitter.com/1/users/show.json',
        data: {screen_name: 'beansight'},
        dataType: 'jsonp',
        success: function(data) {
            $('#twitterFollowers').html(data.followers_count);
        }
    });
	
	$.ajax({
	       url: 'https://graph.facebook.com/Beansight',
	       dataType: 'jsonp',
	       success: function(data) {
	    	   $("#facebookFans").html(data.likes);
	       }
   });
    
    //////////////////////
    // Validator methods
    //////////////////////
	$.validator.addMethod("username",
			function(value,element) {
				return this.optional(element) || /^[a-zA-Z0-9_]{3,16}$/.test(value);
			},
			i18n["usernameValidation"]
		);
	
    //////////////////////
    // Current user reset his activity feed
    //////////////////////	
	$("#resetActivity").click( function() {
		$.get(resetInsightActivityAction(), onResetInsightActivitySuccess);
		return false;
	})
	
	//////////////////////
	// User Options
	//////////////////////
    $('#log-bean').click(function(e) {
    	// This is a fix for the MS IE7 z-index bug 
    	if ($.browser.msie  && parseInt($.browser.version) < 8) {
    		window.location.href = "/login";
    	}
    	
        if($('#log-bean').hasClass('opened')){
            $('#log-bean').removeClass('opened');
            $('#boxlog-bean').slideUp(500);
        }else{
            $('#log-bean').addClass('opened');
            $('#boxlog-bean').slideDown(500);
        }
        return false;
    });
    
    // validate the form
	$("#logBeanForm").validate({
		rules: {
			username: {
				required: true,
				email: true
			},
			password: {
				required: true
			}
		}
	});
	
	// load the user avatar when email filled
	$("#passlogin").focus(function() {
		if($("#pseudologin").valid()) {
			$("#avatarlogin").attr('src', showAvatarSmallFromEmailAction({"email": $("#pseudologin").val()}));
		}
	});
    
	//////////////////////
	// Settings
	//////////////////////
	$("#userSettingsForm").validate({
		rules: {
			username: {
				username: true,
				required: true,
				remote: isUserNameAvailableAction()
			}
		},
		messages: {
			username: {
				remote: jQuery.validator.format(i18n["userNameRemoteValidation"])	
			}
		}
	});

    
	//////////////////////
	//Change Password
	//////////////////////	
	$("#changePasswordForm").validate({
		rules: {
			oldPassword: {
				required: true
			},
			newPassword: {
				required: true,
				minlength: 5
			},
			newPasswordConfirm: {
				required: true,
				minlength: 5,
				equalTo: "#newPassword"
			}
		}
	});
	
	//////////////////////
	//Change Forgot Password
	//////////////////////	
	$("#changeForgotPasswordForm").validate({
		rules: {
			password: {
				required: true,
				minlength: 5
			},
			passwordConfirm: {
				required: true,
				minlength: 5,
				equalTo: "#password"
			}
		}
	});
	
	
	//////////////////////
	// Invitation System
	//////////////////////
    $('#emailInvite').focus(function() { $('#inviteMessage').slideDown('normal');});
    
	$("#inviteForm").validate({
		submitHandler: function(form) {
	        $.getJSON(inviteAction(), $("#inviteForm").serialize(), function() { 
	        	$('#inviteconfirm').slideDown('normal');
	        	$('#inviteTextArea').val('');
	        	$('#emailInvite').val('');
	        	var invitNumber = $('.invitationLeftNumber');
	        	invitNumber.text( invitNumber.text() -1 );
	        	if( invitNumber.text() == 0) {
	        		$('#inviteForm').hide();
	        	}
	            }
	        );
	        return false;
		},
		rules: {
			email : "required email"
		},
		messages: {
			email: i18n.validateContactEmail
		}
	});
    
	//////////////////////
	// Leave your email
	//////////////////////
	$("#leaveYourEmailForm").validate({
		submitHandler: function(form) {
			$.getJSON(leaveYourEmailAction(), $("#leaveYourEmailForm").serialize(), function(data) { 
	        	$('#leaveYourEmailCallBackMsg').text(data.msg);
	        	if (data.hasError == false) {
	        		$('#leaveYourEmailForm').slideUp();
	        	}
	        });
			$('#leaveYourEmailCallBackMsg').text(i18n.sendingEmail);
		},
		rules: {
			email : "required email"
		},
		messages: {
			email: i18n.validateContactEmail
		}
	});
	

	//////////////////////
	// Contact page
	//////////////////////
	$("#contactForm").validate({
		submitHandler: function(form) {
	        $.getJSON(sendToContactAction(), $("#contactForm").serialize(), function(data) { 
	        	if (data.hasError) {
	        		$('#name_error').text(data.name);
	        		$('#from_error').text(data.from);
	        		$('#subject_error').text(data.subject);
	        		$('#message_error').text(data.message);
	        		$('#contact_otherMessage').text(data.otherMessage);
	        	} else {
	        		$('#contact_otherMessage').text(data.otherMessage);
	        		$('#contactForm').slideUp();
	        	}
	        });
		},
		rules: {
			name: "required",
			from : "required email",
			subject : "required",
			message : "required"
		},
		messages: {
			name: i18n.validateContactName,
			from: i18n.validateContactEmail,
			subject: i18n.validateContactsubject,
			message: i18n.validateContactMessage
		}
	});
   
    //////////////////////
	// Create Insight
    //////////////////////
	$("#insightCreationForm").validate({
		rules: {
			endDate: "required",
			insightContent: {
				required: true,
				minlength: 5,
				maxlength: MAX_CHARACTERS_INSIGHT
			},
			insightCreationLang: "required",
			categoryId: "required"
		},
		messages: {
			endDate: i18n.validateEndDate,
			insightContent: {
				required: i18n.validateInsightContent,
				minlength: i18n.validateInsightContentMin,
				maxlength: i18n.validateInsightContentMax
			},
			insightCreationLang: i18n.validateInsightCreationLang,
			categoryId: i18n.validateCategoryId
		}
	});
	
	// Language detection
	$("#insightContent").blur(function() {
		$.ajax({
	        url: 'https://ajax.googleapis.com/ajax/services/language/detect',
	        data: {v: '1.0', q: $("#insightContent").val() },
	        dataType: 'jsonp',
	        success: function(data) {
	        	try {
	        		console.log(data.responseData.language);
	        		// if the user doesn't speak this language
	        		if( $('#userWrittingLanguage').html() != data.responseData.language && $('#userSecondWrittingLanguage').html() != data.responseData.language ) {
	        			// TODO
	        			// Tell him we detected the insight in a language he doesn't speak
	        			// but for now still use his primary language as the insight language
	        			console.log("User doesn't speak this language");
	        		} else {
	        			$('#insightLang').val(data.responseData.language);
	        			$('#insightLangConfidence').val(data.responseData.confidence);
	        		}
	        	} catch(e) {
	        		console.log('Cannot detect language');
	        	}
	        }
	    });	
	})
	
	// Date selection
    $( ".datePicker" ).datepicker({
        //showOn: "button",
    	//buttonImageOnly: true,
        //buttonImage: "/public/images/icon-calendar.png",
        dateFormat: 'yy-mm-dd',
        showOtherMonths: true,
        selectOtherMonths: true,
        minDate: 1
	});
    // Add a special class to our datepickers
    //$(".ui-datepicker").addClass("insightCreationPicker");


	// Progress bar 
    $( "#progressbar" ).progressbar({
    	value: 0
    });
    updateCharacterCount();
    $("#insightContent").keyup(function() { // cannot use change(), keypress()
		updateCharacterCount();
	});
	
	// if key is "Enter", then do not validate the form
	$("#insightContent").keypress(function(e)
	{
		var key = (e.keyCode ? e.keyCode : e.which);
		switch (key)
		{
			// TODO use jQuery UI keycode
			case 13:
			return false;
		}
	});
	
	function split( val ) {
		return val.split( /,\s*/ );
	}
	function extractLast( term ) {
		return split( term ).pop();
	}

	// Tags
	
	// Autocomplete tags
	$( "#taginput" )
	// don't navigate away from the field on tab when selecting an item
	.bind( "keydown", function( event ) {
		if ( event.keyCode === $.ui.keyCode.TAB &&
				$( this ).data( "autocomplete" ).menu.active ) {
			event.preventDefault();
		}
	})
	.autocomplete({
		source: function( request, response ) {
			$.getJSON( tagSuggestAction(), {
				term: extractLast( request.term )
			}, response );
		},
		focus: function() {
			// prevent value inserted on focus
			return false;
		},
		search: function() {
			// custom minLength
			var term = extractLast( this.value );
			if ( term.length < 2 ) {
				return false;
			}
		},
		select: function( event, ui ) {
			this.value = ui.item.value;
			updateTags();
	        validetag();
	        registertags();
			return false;
		}
	})
	.keyup( function(event) { updateTags() })
	.keypress(function(event) {     // if "Return" is pressed, validate the tag.
	    if(event.keyCode=='13'){
	        validetag();
	        registertags();
	        return false;
	    }
	});


    //////////////////////
    // Registration
    //////////////////////
	$("#registerFormBeansight").validate({
		rules: {
			email: {
				required: true,
				email: true,
				remote: isEmailAvailableAction()
			},
			username: {
				username: true,
				required: true,
				remote: isUserNameAvailableAction()
			},
			password: {
				required: true,
				minlength: 5
			},
			passwordconfirm: {
				required: true,
				minlength: 5,
				equalTo: "#registerPassword"
			}
		},
		messages: {
			username: {
				remote: jQuery.validator.format(i18n["userNameRemoteValidation"])	
			}
		}
	});
	
	showRegisterForm('beansight');
	
    //////////////////////
    // Insight Page
    //////////////////////
	// Share on Beansight
	$('#shareOnBeansight-link').click(function() {
		$('#shareOnBeansight-box').slideToggle();
		return false;
	})
	$('#shareOnBeansightForm').submit(function() {
        $.getJSON(shareInsightAction(), $(this).serialize(), function(data) {
        	clearForm($('#shareOnBeansightForm'));
        	var message = i18n.insightShared;
        	if(data.error) {
        		if(data.error === "NotFollowingUserException") { message = i18n.notFollowingUser; }
        		if(data.error === "InsightAlreadySharedException") { message = i18n.insightAlreadyShared; }
        		if(data.error === "CannotFindUser") { message = i18n.cannotFindUser; }
        	}
        	$('#shareConfirmation').html(message);
            }
        );
        return false;
	});
		$( "#userToShareTo" ).autocomplete({
	    source: favoriteUserSuggestAction(),
	    minLength: 1
	});
	
	// Sugest tags
	$('#showMoreTags').click( function() {
		$('#moreTags').show();
		return false;
	});
	
	// Submit action for add comment form
	$("#addCommentForm").validate({
		submitHandler: function(form) {
			$(".ajaxloader").show();
	        $.ajax( {
	            url: addCommentAction(),
	            data: $("#addCommentForm").serialize(),
	            success: onAddCommentSuccess
	        } );
		    return false;
		},
		rules: {
			content: {
		        required: true,
		        minlength: 5
		      }
		},
		messages: {
			content: i18n.newCommentMinSize
		}
	});
	
	// tooltip
	$(".voteWidgetLarge .loginTooltip").tooltip({showURL: false});
	
    //////////////////////
    // User Page
    //////////////////////
    $('#link-sendmail').click(function() {
    	toggleSendMessage()
        return false;
    });
    
    $('#sendMessageForm').submit(function() {
    	toggleSendMessage()
        $.getJSON(sendMessageAction(), $(this).serialize(), function() { 
        	$('#messageUserContent').val('');
        	// TODO use a generic confirmation method
        	$('#messagesentconfirm').show();
            }
        );
        return false;
    });
    
    function toggleSendMessage() {
    	$('#link-sendmail').toggleClass('open');
        $('#boxlink-sendmail').slideToggle(500);
    }
    
	$("#updateUserRealNameForm").validate({
		submitHandler: function(form) {
			$.getJSON(updateUserRealNameAction(), $("#updateUserRealNameForm").serialize(), function(data) {
				if(data.realName !== "") {
					data.realName = "(" + data.realName + ")";
				}
	        	$("#userRealName").html(data.realName);
	        });
			$("#editRealNameZone").hide('normal');
		},
		rules: {
			realName: {
				maxlength: 30
			}
		},
		messages: {
			realName: i18n.validationRealName
		}
	});
	
	$("#updateUserDescriptionForm").validate({
		submitHandler: function(form) {
			$.getJSON(updateUserDescriptionAction(), $("#updateUserDescriptionForm").serialize(), function(data) { 
	        	$("#userDesctiption").html(data.description);
	        });
			$("#editDescriptionZone").hide('normal');
		},
		rules: {
			description: {
				maxlength: 120
			}
		},
		messages: {
			description: i18n.validationDescription
		}
	});
	
	$( "#modal-crop" ).dialog({
		height: 	500,
		width:		600,
		modal: 		true,
		open:		onOpenModalCrop,
		beforeclose:onCloseModalCrop,
		autoOpen: 	false,
		resizable:	false,
		draggable:	false
	});
	
	$('#cropAvatar').click(function() {  
		$('#modal-crop').dialog('open');
		return false;
    }); 

	$("#cropForm").submit(function() {
		$.post(cropImageAction(), $("#cropForm").serialize(), refreshAvatarImage);
		$('#modal-crop').dialog('close');
		return false;
	});
	
	$("#editRealName").click(function() {
		$("#editRealNameZone").toggle('normal');
		return false;
	});
	$("#editDescription").click(function() {
		$("#editDescriptionZone").toggle('normal');
		return false;
	});
	$("#editAvatar").click(function() {
		$("#editAvatarZone").toggle('normal');
		return false;
	});
	
	//////////////
	// Search
	//////////////
    $('#searchFilterCategory').selectbox().bind('change', function() {
        window.location.href = $(this).val();
    })
	
	//////////////
	// Insights list
	//////////////

    // Vote selection buttons
    $("#radioSortByFilter").buttonset();
    // Vote selection buttons
    $("#radioVotefilter").buttonset();
    // category selection custom
    $("#filterCategory").selectbox();
	
	$("#filterCategory").change(function() {
		loadInsights(getInsightsAction, generateGetInsightsArguments);
		bindCurrentState(reloadInsightsAction( generateGetInsightsArguments() ));
		return false;
	});
	$('input[name=VoteGroup]').change(function() {
		loadInsights(getInsightsAction, generateGetInsightsArguments);
		bindCurrentState(reloadInsightsAction( generateGetInsightsArguments() ));
		return false;
	});
	$('input[name=SortByGroup]').change(function() {
		loadInsights(getInsightsAction, generateGetInsightsArguments);
		bindCurrentState(reloadInsightsAction( generateGetInsightsArguments() ));
		return false;
	});
	
	$("#moreInsights").click( function() {
		loadMoreInsights(getInsightsAction, generateGetInsightsArguments);
		bindCurrentState(reloadInsightsAction( generateGetInsightsArguments() ));
	    return false;
	});
	
	//////////////
	// User Insights list
	//////////////
	
    // category selection custom
    $("#userInsightsFilterCategory").selectbox();
    
	$("#userInsightsFilterCategory").change(function() {
		loadInsights(getUserInsightsAction, generateGetUserInsightsArguments);
		bindCurrentState(reloadUserInsightsAction( generateGetUserInsightsArguments() ));
		return false;
	});
	
	$('input[name=userVoteGroup]').change(function() {
		loadInsights(getUserInsightsAction, generateGetUserInsightsArguments);
		bindCurrentState(reloadUserInsightsAction( generateGetUserInsightsArguments() ));
		return false;
	});
	
	$("#moreUserInsights").click( function() {
		loadMoreInsights(getUserInsightsAction, generateGetUserInsightsArguments);
		bindCurrentState(reloadUserInsightsAction( generateGetUserInsightsArguments() ));
	    return false;
	});
	
	// Check if currently on a mobile, if so, display a message
	(function(a,b){if(/android|avantgo|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|symbian|treo|up\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|e\-|e\/|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(di|rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|xda(\-|2|g)|yas\-|your|zeto|zte\-/i.test(a.substr(0,4))){displayMobileMessage()}})(navigator.userAgent||navigator.vendor||window.opera);
	
});

