/** Current user agree an insight */
function agree(insightUniqueId) {
    $.getJSON(agreeAction, {"insightUniqueId": insightUniqueId}, onVoteSuccess);
    
//    var insightContainer = $(".insight_" + insightUniqueId);
//   	insightContainer.removeClass("voteDisagree").addClass("voteAgree");
}

/** Current user disagree an insight */
function disagree(insightUniqueId) {
    $.getJSON(disagreeAction, {"insightUniqueId": insightUniqueId}, onVoteSuccess);

//    var insightContainer = $(".insight_" + insightUniqueId);
//   	insightContainer.addClass("voteDisagree").removeClass("voteAgree");
}

/** Callback after a vote is done */
function onVoteSuccess(data) {
	console.log("vote success");
}

/** Load the insight list */
function getInsights(sort) {
    $.getJSON(getInsightsAction, {sort: sort}, onGetInsightsSuccess);
}

function onGetInsightsSuccess(data) {
	$("#insightList")
		.html( $("#insightTemplate").tmpl( data ) )
		.append('<li data-role="list-divider"><span class="loadmore">more</span></a></li>')
		.listview('refresh'); 
	
	// associate to each one of these insights the click action
	$(".insight-link").each(function(index, element) {
		// Apply the insight info to the Insight page
		// Get the insight info
		var content = $(".content", element).html();
		var endDate = $(".endDate", element).html();
		var uniqueId = $(element).attr("data-uniqueid", uniqueId);
		// change the content of the insight
		$(element).click(function() {
			$("#page-insight").attr("data-uniqueid", uniqueId);
			getInsight(uniqueId);
			
			$("#insight-endDate").html(endDate);
			$("#insight-content").html(content);
			$("#btn-disagree").removeClass("ui-btn-active");
			$("#btn-agree").removeClass("ui-btn-active");
		});
		
	});
}

//function getMoreInsights() {
//    $.getJSON(getInsightsAction, {from: 20} ,onGetMoreInsightsSuccess);
//}
//
//function onGetMoreInsightsSuccess(data) {
//	$("#insightList")
//		.append( $("#insightTemplate").tmpl( data ) )
//		.listview('refresh'); 
//}

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
		if (data.lastUserVote === "AGREE") {
			$("#btn-agree").addClass("ui-btn-active");
		} else if (data.lastUserVote === "DISAGREE") {
			$("#btn-disagree").addClass("ui-btn-active");
		}
	}
}

// Execute scripts after the document creation
$(document).ready(function() {
	
	$("#filter-upcomming").click(function() {
		getInsights("incoming");
	});
	$("#filter-popularity").click(function() {
		getInsights("trending");
	})
	$("#filter-update").click(function() {
		getInsights("updated");
	});
	
	$("#btn-agree").click(function() {
		agree($("#page-insight").attr("data-uniqueid"));
	});
	$("#btn-disagree").click(function() {
		disagree($("#page-insight").attr("data-uniqueid"));
	});
});