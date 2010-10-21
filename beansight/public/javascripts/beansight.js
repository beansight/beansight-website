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
    updateAgreeDisagreeCount(data.id, data.updatedAgreeCount, data.updatedDisagreeCount);
}

/** Update the counts of an insight, given new counts */
function updateAgreeDisagreeCount(id, agreeCount, disagreeCount) {
    $("#agreeCount_" + id).text(agreeCount);
    $("#disagreeCount_" + id).text(disagreeCount);
}



// Overload jQuery error

/** Called when an AJAX request returns an error */
$("#error").ajaxError(function(event, request, settings){
    $(this).text('Sorry, an error occured during last action.');
});


/** Submit action for insight creation form */
$('#createInsightForm').submit(function() {
    $.post("@{Application.createInsight()}", $(this).serialize(), onCreateInsightSuccess);
    return false;
});

/** callback for insight creation */
function onCreateInsightSuccess(msg) {
    alert('TODO:' + msg);
    clearForm();
    // TODO prepend the list with the new insight
    // .prepend()
}

function clearForm() {
    $(':input','#createInsightForm')
     .not(':button, :submit, :reset, :hidden')
     .val('')
     .removeAttr('checked')
     .removeAttr('selected');
}