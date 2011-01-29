// Size of an insight
var MAX_CHARACTERS_INSIGHT = 100;

//////////////////////
// Cufon Settings
//////////////////////
Cufon.set('fontFamily', 'got-rnd-book').replace('.cuf-grb', {hover: true});
Cufon.set('fontFamily', 'got-rnd-med').replace('.cuf-grm', {hover: true});
Cufon.set('fontFamily', 'got-rnd-bold').replace('.cuf-grs', {hover: true});
Cufon.set('fontFamily', 'got-rnd-med').replace('.shadowtwit', {textShadow: '#54d5f6 1px 1px'}, {hover: true});
Cufon.set('fontFamily', 'got-rnd-med').replace('.shadowfb', {textShadow: '#223974 1px 1px'}, {hover: true});
//Cufon.set('fontFamily', 'myriad pro').replace('#sidebar-signup h4', {textShadow: '#ee7c63 1px 1px'}, {fontWeight:'400'});

//////////////////////
// Actions on insights
//////////////////////

function testtesttest() {return false;}

/** Current user agree an insight */
function agree(insightUniqueId) {
    $.getJSON(agreeAction({'insightUniqueId': insightUniqueId}), onVoteSuccess);
    
    var insightContainer = $(".insight_" + insightUniqueId);
   	insightContainer.removeClass("voteDisagree").addClass("voteAgree");

   	return false;
}

/** Current user disagree an insight */
function disagree(insightUniqueId) {
    $.getJSON(disagreeAction({'insightUniqueId': insightUniqueId}), onVoteSuccess);

    var insightContainer = $(".insight_" + insightUniqueId);
   	insightContainer.addClass("voteDisagree").removeClass("voteAgree");

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

// Overload jQuery error
/** Called when an AJAX request returns an error */
$("#error").ajaxError(function(event, request, settings){
    $(this).text('Sorry, an error occured during last action.');
});

// Execute scripts after the document creation
$(document).ready(function() {
	
	/** Current user reset his activity feed*/
	$("#resetActivity").click( function() {
		$.get(resetInsightActivityAction(), onResetInsightActivitySuccess);
		return false;
	})
	
	//////////////////////
	// User Options
	//////////////////////
    $('#btnlogin').addClass('open');
    $('#btnlogin').click(function(e) {
        if($('#btnlogin').hasClass('open')){
            $('#btnlogin').removeClass('open');
            $('#boxlogin').slideDown(500);
        }else{
            $('#btnlogin').addClass('open');
            $('#boxlogin').slideUp(500);
        }
        return false;
    });
    
    // zone identification fade 
    $('.item-loginbox').fadeOut(200);
    $('#boxlog-bean').fadeIn(200);
    $('.listlogin li a').click(function(e) {
        $('.listlogin li a').removeClass('current');
        $(this).addClass('current');
        $('.item-loginbox').fadeOut(200);
        $('#box'+$(this).attr('id')).delay(400).fadeIn(200);
        $('#boxlogin .list-logins').animate({
            height: $('#box'+$(this).attr('id')).height()
            }, 500
        );
        return false;
    });

	//////////////////////
	// Settings
	//////////////////////
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
    
	//////////////////////
	// Invitation System
	//////////////////////
    $('#emailInvite').focus(function() { $('#inviteMessage').slideDown('normal');});
    
    $('#inviteForm').submit(function() {
        $.getJSON(inviteAction(), $(this).serialize(), function() { 
        	$('#inviteconfirm').slideDown('normal');
        	$('#inviteTextArea').val('');
        	$('#emailInvite').val('');
            }
        );
        return false;
    });
    
    $('#sendMessageForm').submit(function() {
        $.getJSON(sendMessageAction(), $(this).serialize(), function() { 
        	$('#messageUserContent').val('');
        	$('#messagesentconfirm').show();
            }
        );
        return false;
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
				maxlength: 140
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
	$("#registerForm").validate({
		rules: {
			email: {
				required: true,
				email: true
			},
			username: {
				required: true,
				minlength: 3
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
		}
	});

    //////////////////////
    // Insight Page
    //////////////////////
	$('#shareOnBeansight').click(function() {
		$('#shareOnBeansightForm').slideDown();
		return false;
	});
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
	})
	
	/** Sugest tags */
	$('#showMoreTags').click( function() {
		$('#moreTags').show();
		return false;
	});
	
	/** Submit action for add comment form */
	$("#addCommentForm").validate({
		submitHandler: function(form) {
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

	$.ajax({
        url: 'http://api.twitter.com/1/users/show.json',
        data: {screen_name: 'beansight'},
        dataType: 'jsonp',
        success: function(data) {
            $('#twitterFollowers').html(data.followers_count);
        }
    });

	$( "#userToShareTo" ).autocomplete({
	    source: favoriteUserSuggestAction(),
	    minLength: 2
	});
	
	$.ajax({
	       url: 'https://graph.facebook.com/Beansight',
	       dataType: 'jsonp',
	       success: function(data) {
	    	   $("#facebookFans").html(data.likes);
	       }
	   });
});
