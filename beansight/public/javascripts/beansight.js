// Insight Vote

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
	
	$("#insightCreationLangEn").click( function() {
		$("#insightCreationLang").val("en");
		return false;
	});
	$("#insightCreationLangFr").click( function() {
		$("#insightCreationLang").val("fr");
		return false;
	});
	
});