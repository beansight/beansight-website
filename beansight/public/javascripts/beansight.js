//////////////////////
// Actions on insights
//////////////////////

/** Current user agree an insight */
function agree(insightId) {
    $.getJSON(agreeAction({'insightId': insightId}), onVoteSuccess);
}

/** Current user disagree an insight */
function disagree(insightId) {
    $.getJSON(disagreeAction({'insightId': insightId}), onVoteSuccess);
}

/** Callback after a vote is done */
function onVoteSuccess(data) {
    updateAgreeDisagreeCount(data.id, data.updatedAgreeCount, data.updatedDisagreeCount, data.voteState);
}

/** Update the counts of an insight, given new counts */
function updateAgreeDisagreeCount(id, agreeCount, disagreeCount, voteState) {
	var insightContainer = $(".insight_" + id);
    $(".agreeCount", insightContainer).text(agreeCount);
    $(".disagreeCount", insightContainer).text(disagreeCount);
    
    if(voteState == "agree") {
    	insightContainer.removeClass("voteDisagree").addClass("voteAgree");
    } else {
    	insightContainer.addClass("voteDisagree").removeClass("voteAgree");
    }
}

function toggleFollowingInsight(insightId) {
	$.getJSON(toggleFollowingInsightAction({'insightId': insightId}), onToggleFollowingInsightSuccess);
}

/** Callback after a follow of insight is done */
function onToggleFollowingInsightSuccess(data) {
	var favicon = $(".icon.favorite", ".insight_" + data.id);
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
    $("#signupLink").click(function() {
        $("#loginBox").hide();
        var box = $("#signupBox");
        if (box.is(":visible")) {
            box.fadeOut();
        } else {
            box.fadeIn();
        }
        return false;
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

	
});