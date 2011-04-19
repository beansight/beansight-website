// Execute scripts after the document creation

/** Current user agree an insight */
function agree(insightUniqueId) {
	setActiveVoteButton(insightUniqueId, "agree");
    $.getJSON(agreeAction, {"insightUniqueId": insightUniqueId}, onVoteSuccess);
}

/** Current user disagree an insight */
function disagree(insightUniqueId) {
	setActiveVoteButton(insightUniqueId, "disagree");
    $.getJSON(disagreeAction, {"insightUniqueId": insightUniqueId}, onVoteSuccess);
}

/** Callback after a vote is done */
function onVoteSuccess(data) {

}

function removeActiveVoteButton(insightUniqueId) {
	var $page = 'div[data-insightid="'+ insightUniqueId +'"]';
	$('.btn-agree', $page).removeClass("ui-btn-active");
	$('.btn-disagree', $page).removeClass("ui-btn-active");
}

function setActiveVoteButton(insightUniqueId, voteState) {
	removeActiveVoteButton(insightUniqueId);
	
	var $page = 'div[data-insightid="'+ insightUniqueId +'"]';
	if (voteState === "agree") {
		$('.btn-agree', $page).addClass("ui-btn-active");
	} else if (voteState === "disagree") {
		$('.btn-disagree', $page).addClass("ui-btn-active");
	}
}

/** Load the insight data */
function getInsight(uniqueId) {
	$.mobile.pageLoading();
    $.getJSON(getInsightAction, {insightUniqueId: uniqueId}, onGetInsightSuccess);
}

function onGetInsightSuccess(data) {
	$.mobile.pageLoading( true );
	var $page = $( '[data-insightid="' + data.uniqueId + '"]');
	$(".insight-creator", $page).html(data.creator);
	$(".insight-content", $page).html(data.content);
	$(".insight-endDate", $page).html(data.endDate);
	$(".insight-agreeCount", $page).html(data.agreeCount);
	$(".insight-disagreeCount", $page).html(data.disagreeCount);
	$(".insight-creationDate", $page).html(data.creationDate);	
	
	if (data.lastUserVote) {
		setActiveVoteButton(data.uniqueId, data.lastUserVote);
	}
}

$(document).ready(function() {
	
	$(".btn-agree").live('click', function() {
		agree($(this).attr("data-insightid"));
		return false;
	});

	$(".btn-disagree").live('click', function() {
		disagree($(this).attr("data-insightid"));
		return false;
	});
	
	$('div[data-role=page]').live('pagecreate', function(event) {
		// populate the data-insightid with the unique Id of the insight, taken from the URL
		var $page = $(event.target);
		var insightId = $page.attr("data-url").split("m/prediction/")[1];
		$page.attr("data-insightid", insightId);
		$(".btn-agree", $page).attr("data-insightid", insightId);
		$(".btn-disagree", $page).attr("data-insightid", insightId);
		console.log(insightId);
		getInsight(insightId);
	});
});