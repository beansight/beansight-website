*{ Display the Sharing options }*
*{ @param insight : the insight to share }*
*{ @param boolean noBeansight : true do hide the share on beansight button }*

<p class="sharelink">
    <span class="labelshare">&{'showInsight.share'}</span>
    #{if _noBeansight == null || _noBeansight == false }
    #{isConnected}
    <a href="#" class="itemshare beansightshare" id="shareOnBeansight-link">Beansight</a>
    #{/isConnected}
    #{/if}
    <a href="#" class="itemshare twittershare" id="shareOnTwitter-link">Twitter</a>
    <a href="#" class="itemshare facebookshare" id="shareOnFacebook-link">Facebook</a>
</p>

#{if _noBeansight == null || _noBeansight == false }
   <div id="shareOnBeansight-box" style="display:none; margin-top: 15px;" class="pinnedBox">
   <form id="shareOnBeansightForm">
       <p>&{'shareInsight.searchBeansightUser'}</p>
       <input type="text" name="userName" id="userToShareTo" />
       <input type="hidden" name="insightUniqueId" value="${_insight.uniqueId}" />
       <input type="submit" value="&{'shareInsight.sharebutton'}"/>
   </form>
   <span id="shareConfirmation"></span>
   </div>
#{/if} 

<span id="shareOnTwitter" class="shareItem" style="display:none;" >
	<script type="text/javascript">
    	var tweet = "${_insight.content}" + " #beansight";
	</script>
    <a href="http://twitter.com/share" class="twitter-share-button" data-text="${_insight.content}" + " - http://beansight.com/prediction/" + "${_insight.uniqueId}" + " #beansight" data-count="horizontal">Tweet</a>
    <script type="text/javascript" src="http://platform.twitter.com/widgets.js"></script>
    <script type="text/javascript">
    	$('a[data-text]').each(function(){
             $(this).attr('data-text', tweet);
        });
        twttr.events.bind('tweet', function() {
          AKSdk.call_action('twitter', true);
        });
    </script>
</span>

<span id="shareOnFacebook" class="shareItem" style="display:none;">
    <script src="http://connect.facebook.net/en_US/all.js#xfbml=1"></script>
    <fb:like layout="button_count" show_faces="false" width="50"></fb:like>
    <script type="text/javascript">
    FB.Event.subscribe('edge.create',
	    function(response) {
	        AKSdk.call_action('facebook', true);
	    });
    </script>

</span>