// Size of an insight
var MAX_CHARACTERS_INSIGHT = 140;

//////////////////////
// Actions on insights
//////////////////////

/** Current user agree an insight */
function agree(insightUniqueId) {
    $.getJSON(agreeAction({'insightUniqueId': insightUniqueId}), onVoteSuccess);
}

/** Current user disagree an insight */
function disagree(insightUniqueId) {
    $.getJSON(disagreeAction({'insightUniqueId': insightUniqueId}), onVoteSuccess);
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
}

/** Callback after a follow of insight is done */
function onToggleFollowingInsightSuccess(data) {
	var favicon = $(".icon.favorite", ".insight_" + data.uniqueId);
	if(data.follow) {
		favicon.addClass("active").removeClass("inactive");
	} else {
		favicon.addClass("inactive").removeClass("active");
	}
	
}

function toggleFollowingUser(userId) {
	$.getJSON(toggleFollowingUserAction({'userId': userId}), onToggleFollowingUserSuccess);
}

/** Callback after a follow of user is done */
function onToggleFollowingUserSuccess(data) {
	var favicon = $(".icon.favorite", ".user_" + data.id);
	if(data.follow) {
		favicon.addClass("active").removeClass("inactive");
	} else {
		favicon.addClass("inactive").removeClass("active");
	}
	
}

/** Current user reset his activity feed*/
function resetInsightActivity() {
	$.get(resetInsightActivityAction(), onResetInsightActivitySuccess);
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

/** show the more tag suggestion input field*/
function showAddMoreTags() {
	$('#moreTags').show();
}

/** callback for comment addition */
function onAddCommentSuccess(data) {
    $("#commentList").append( '<li>' + data.user + " (" + data.since + ")" + "<br/>" + data.content + '</li>');
    clearForm('#addCommentForm');
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
	//////////////////////
	// User Options
	//////////////////////
    $("#loginLink").click(function() {
        var box = $("#loginBox");
        if (box.is(":visible")) {
            box.fadeOut();
        } else {
            box.fadeIn();
        }
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
            }
        );
        return false;
    });
	
    //////////////////////
    // Insight creation
    //////////////////////
	$('#insightCreationLangEn').click( function() {
		$('.insightCreationLangChoose img').removeClass('selected');
		$('#insightCreationLangEn img').addClass('selected');
		$('#insightCreationLang').val('en');
		return false;
	});
	$('#insightCreationLangFr').click( function() {
		$('.insightCreationLangChoose img').removeClass('selected');
		$('#insightCreationLangFr img').addClass('selected');
		$('#insightCreationLang').val('fr')
			.addClass('selected');
		return false;
	});
	
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
	
	$(".datePicker").datepicker({ 
        dateFormat: 'yy-mm-dd',
        showOtherMonths: true,
        selectOtherMonths: true,
        minDate: 0
	} );

	$( "#progressbar" ).progressbar({
		value: 0
	});

	// Progress bar 
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
			case 13:
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
	
	/** Submit action for add comment form */
	$('#addCommentForm').submit(function() {
	    $.getJSON("@{Application.addComment(insight.id)}", $(this).serialize(), onAddCommentSuccess);
	    return false;
	});

	// When a autocomplete suggestion is selected, update the country <select> 
	$( "#userToShareTo" ).autocomplete({
	    source: favoriteUserSuggestAction(),
	    minLength: 2,
	    select: function(event, ui) {
	        //countrySelect.val(ui.item.country);
	        //placeMarkerOnSelectionMap( new google.maps.LatLng(ui.item.latitude, ui.item.longitude) );
	    }
	});
	
});