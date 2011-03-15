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

	/* make sure the taginput in not a single comma, then clear it */
    function validetag(){
        if($('#taginput').val() != ',' && $('#taginput').val() != ';' && $('#taginput').val().replace(/( |,|;)/ig, '').length > 0 ) {
            $('#newtag').attr('id', '');
            $('#taginput').val('');
        }
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
		        $('<a href="#" id="newtag">'+$('#taginput').val().replace(/(;|,)/ig, '')+'</a>').appendTo(".listtags");
		        clicktags();
		    }
		    // if a comma has been entered, then delete the newtag
		    if(taginput.val().indexOf(',')!=-1 || taginput.val().indexOf(';')!=-1){ // if a comma is entered,
		        validetag();
        	}else{ // in any other case, update the newtag
            	$('#newtag').html('<span>'+taginput.val().replace(/(;|,)/ig, '')+'</span>');
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
 * replace the insight list with a new one
 */
function loadInsights() {
	insightsFrom = 0;
	$.get( getInsightsAction( generateGetInsightsArguments() ), function(content) {
		$('#insightList').html(content);
		//insightsFrom += NUMBER_INSIGHTS_INSIGHTPAGE;
	});
}

/**
 * append to the insight list more results
 */
function loadMoreInsights() {
	insightsFrom += NUMBER_INSIGHTS_INSIGHTPAGE;
	$.get( getInsightsAction( generateGetInsightsArguments() ), function(content) {
		$('#insightList').append(content);
	});
}

/**
 * append to the insight list more results
 */
function reloadInsights(path) {
	$.get( path, function(content) {
		$('#insightList').html(content);
	});
}

function bindCurrentState() {
	// setting bindingTime as true insure that no event will be triggered when setting the hash
	bindingTime = true;
	$.History.setHash(reloadInsightsAction( generateGetInsightsArguments() ));
}
/**
 * @returns the arguments needed for getInsightsAction(); 
 */
function generateGetInsightsArguments() {
	var sortBy = $('input[name=SortByGroup]:checked').val(); 
	var cat = $('#filterCategory').val();
	var filterVote = $('input[name=VoteGroup]:checked').val(); 
	return {'from':insightsFrom, 'sortBy': sortBy,  'cat':cat, 'filterVote':filterVote, 'topic':filterTopic};
}

function refreshFilters(str) {
	sortBy = gup(str, "sortBy"); // incoming, trending, updated
	voteFilter = gup(str, "filterVote"); // all, voted, notVoted
	
	$("input[type=radio]").val([sortBy, voteFilter]);
	
	category = gup(str, "cat");
	$("#filterCategory option[value='" + category + "']").attr('selected', 'selected');
	
	// call this to have the jqueryui refreshed and see the change
	$(':radio').button('refresh');
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
        if($('#log-bean').hasClass('opened')){
            $('#log-bean').removeClass('opened');
            $('#boxlog-bean').slideUp(500);
        }else{
            $('#log-bean').addClass('opened');
            $('#boxlog-bean').slideDown(500);
            
            $('#boxlog-twit').slideUp(400);
            $('#boxlog-fb').slideUp(400);
            $('#log-twit').removeClass('opened');
            $('#log-fb').removeClass('opened');
        }
        return false;
    });
    
    $('#log-twit').click(function(e) {
        if($('#log-twit').hasClass('opened')){
            $('#log-twit').removeClass('opened');
            $('#boxlog-twit').slideUp(500);
        }else{
            $('#log-twit').addClass('opened');
            $('#boxlog-twit').slideDown(500);
            
            $('#boxlog-bean').slideUp(400);
            $('#boxlog-fb').slideUp(400);
            $('#log-bean').removeClass('opened');
            $('#log-fb').removeClass('opened');
        }
        return false;
    });

    $('#log-fb').click(function(e) {
        if($('#log-fb').hasClass('opened')){
            $('#log-fb').removeClass('opened');
            $('#boxlog-fb').slideUp(500);
        }else{
            $('#log-fb').addClass('opened');
            $('#boxlog-fb').slideDown(500);
            
            $('#boxlog-bean').slideUp(400);
            $('#boxlog-twit').slideUp(400);
            $('#log-bean').removeClass('opened');
            $('#log-twit').removeClass('opened');
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
    // Insight creation
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
	        		// if this user doesn't speak this language, it's strange.
	        		if( $('#userWrittingLanguage').html() != data.responseData.language) {
	        			// TODO
	        			// tell him we detected this insight in this language.
	        			// ask him if this language is right, and if so, does he speak this language?
	        			//$('#strangeLanguage').slideDown();
	        		} 
	        		$('#insightLang').val(data.responseData.language);
	        		$('#insightLangConfidence').val(data.responseData.confidence);
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
        	$('#shareOnBeansightForm').slideUp();
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
		loadInsights( reloadInsightsAction( generateGetInsightsArguments() ) );
		bindCurrentState();
		return false;
	});
	$('input[name=VoteGroup]').change(function() {
		loadInsights();
		bindCurrentState();
		return false;
	});
	$('input[name=SortByGroup]').change(function() {
		loadInsights();
		bindCurrentState();
		return false;
	});
	
	$("#moreInsights").click( function() {
		loadMoreInsights();
		bindCurrentState();
	    return false;
	});

	
});

