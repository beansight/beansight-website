var INSIGHT_NUMBER_TO_LOAD = 10;

var beansightConf = {};
beansightConf.from = INSIGHT_NUMBER_TO_LOAD;
beansightConf.sort = "incoming";

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
	$('.insight-agreeCount', '.btn-agree[data-insightid="'+ data.uniqueId +'"]').html(data.updatedAgreeCount);
	$('.insight-disagreeCount', '.btn-disagree[data-insightid="'+ data.uniqueId +'"]').html(data.updatedDisagreeCount);
	
}

/** Load the insight list */
function getInsights() {
    $.getJSON(getInsightsAction, {sort: beansightConf.sort, number: INSIGHT_NUMBER_TO_LOAD}, onGetInsightsSuccess);
    $.mobile.pageLoading();
}

function onGetInsightsSuccess(data) {
	$("#insightList")
		.html( $("#insightTemplate").tmpl( data ) )
		.listview('refresh'); 
	$.mobile.pageLoading(true);
}

function getMoreInsights() {
    $.getJSON(getInsightsAction, {sort: beansightConf.sort, from: beansightConf.from, number: INSIGHT_NUMBER_TO_LOAD} ,onGetMoreInsightsSuccess);
    beansightConf.from += INSIGHT_NUMBER_TO_LOAD;
	$.mobile.pageLoading();
}

function onGetMoreInsightsSuccess(data) {
	$.mobile.pageLoading(true);
	$("#insightList")
		.append( $("#insightTemplate").tmpl( data ) )
		.listview('refresh'); 
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

// Execute scripts after the document creation
$(document).ready(function() {
	
	$("#filter-upcomming").click(function() {
		beansightConf.from = INSIGHT_NUMBER_TO_LOAD;
		beansightConf.sort = "incoming";
		getInsights();
	});
	$("#filter-popularity").click(function() {
		beansightConf.from = INSIGHT_NUMBER_TO_LOAD;
		beansightConf.sort = "trending";		
		getInsights();
	})
	$("#filter-update").click(function() {
		beansightConf.from = INSIGHT_NUMBER_TO_LOAD;		
		beansightConf.sort = "updated";		
		getInsights();
	});
	
	$(".btn-agree").live('click', function() {
		agree($(this).attr("data-insightid"));
	});
	$(".btn-disagree").live('click', function() {
		disagree($(this).attr("data-insightid"));
	});
	
	$("#btn-more").click(function() {
		getMoreInsights();
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