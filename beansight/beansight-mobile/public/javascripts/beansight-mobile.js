var INSIGH_TNUMBER_TO_LOAD = 10;

var beansightConf = {};
beansightConf.from = INSIGH_TNUMBER_TO_LOAD;
beansightConf.sort = "incoming";

var sort = "incomming"

/** Current user agree an insight */
function agree(insightUniqueId) {
    $.getJSON(agreeAction, {"insightUniqueId": insightUniqueId}, onVoteSuccess);
	setActiveVoteButton("agree");    
}

/** Current user disagree an insight */
function disagree(insightUniqueId) {
    $.getJSON(disagreeAction, {"insightUniqueId": insightUniqueId}, onVoteSuccess);
	setActiveVoteButton("disagree");    
}

/** Callback after a vote is done */
function onVoteSuccess(data) {
	setActiveVoteButton(data.voteState);
}

/** Load the insight list */
function getInsights() {
    $.getJSON(getInsightsAction, {sort: beansightConf.sort, number: INSIGH_TNUMBER_TO_LOAD}, onGetInsightsSuccess);
}

function onGetInsightsSuccess(data) {
	$("#insightList")
		.html( $("#insightTemplate").tmpl( data ) )
		.listview('refresh'); 
	associateInsightClickAction();
	
}

function getMoreInsights() {
    $.getJSON(getInsightsAction, {sort: beansightConf.sort, from: beansightConf.from, number: INSIGH_TNUMBER_TO_LOAD} ,onGetMoreInsightsSuccess);
    beansightConf.from += INSIGH_TNUMBER_TO_LOAD;
}

function onGetMoreInsightsSuccess(data) {
	$("#insightList")
		.append( $("#insightTemplate").tmpl( data ) )
		.listview('refresh'); 
	associateInsightClickAction();	
}

function associateInsightClickAction() {
	// associate to each one of these insights the click action
	$(".insight-link").each(function(index, element) {
		// Get the insight info
		var content = $(".content", element).html();
		var endDate = $(".endDate", element).html();
		var uniqueId = $(element).attr("data-uniqueid");
		// change the content of the insight page
		$(element).click(function() {
			$("#page-insight").attr("data-uniqueid", uniqueId);
			getInsight(uniqueId);
			
			$("#insight-endDate").html(endDate);
			$("#insight-content").html(content);
			removeActiveVoteButton();
		});
	});
}

function removeActiveVoteButton() {
	$("#btn-disagree").removeClass("ui-btn-active");
	$("#btn-agree").removeClass("ui-btn-active");
}

function setActiveVoteButton(voteState) {
	removeActiveVoteButton();
	if (voteState === "agree") {
		$("#btn-agree").addClass("ui-btn-active");
	} else if (voteState === "disagree") {
		$("#btn-disagree").addClass("ui-btn-active");
	}
}

/** Load the insight data */
function getInsight(uniqueId) {
    $.getJSON(getInsightAction, {insightUniqueId: uniqueId}, onGetInsightSuccess);
}

function onGetInsightSuccess(data) {
	console.log(data);
	$("#insight-creator").html(data.creator);
	$("#insight-agreeCount").html(data.agreeCount);
	$("#insight-disagreeCount").html(data.disagreeCount);
	
	if (data.lastUserVote) {
		setActiveVoteButton(data.lastUserVote);
	}
}

// Execute scripts after the document creation
$(document).ready(function() {
	
	$("#filter-upcomming").click(function() {
		beansightConf.from = INSIGH_TNUMBER_TO_LOAD;
		beansightConf.sort = "incoming";
		getInsights();
	});
	$("#filter-popularity").click(function() {
		beansightConf.from = INSIGH_TNUMBER_TO_LOAD;
		beansightConf.sort = "trending";		
		getInsights();
	})
	$("#filter-update").click(function() {
		beansightConf.from = INSIGH_TNUMBER_TO_LOAD;		
		beansightConf.sort = "updated";		
		getInsights();
	});
	
	$("#btn-agree").click(function() {
		agree($("#page-insight").attr("data-uniqueid"));
	});
	$("#btn-disagree").click(function() {
		disagree($("#page-insight").attr("data-uniqueid"));
	});
	
	$("#btn-more").click(function() {
		getMoreInsights();
	});
});