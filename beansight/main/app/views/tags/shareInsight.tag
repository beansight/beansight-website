*{ Display the Sharing options }*
*{ @param insight : the insight to share }*

<div class="sharezone">
	<div><a href="#" class="uiButton" id="shareInsight">&{'shareInsight.sharethisinsight'}</a></div>
	
	<div class="sharebox" id="sharezone" style="display:none;">
	       #{isConnected}
	       <span id="shareOnBeansight">
	       <a href="#" id="shareOnBeansight-link" class="uiButton">&{'shareInsight.shareonbeansight'}</a>
           </span>
           <div id="shareOnBeansight-box" style="display:none;" class="pinnedBox">
	           <form id="shareOnBeansightForm">
	               <p>&{'shareInsight.searchBeansightUser'}</p>
	               <input type="text" name="userName" id="userToShareTo" />
	               <input type="hidden" name="insightUniqueId" value="${_insight.uniqueId}" />
	               <input type="submit"/>
	           </form>
	           <span id="shareConfirmation"></span>
           </div>
           #{/isConnected}
           
           <span id="shareOnTwitter">
	       <a href="http://twitter.com/share" class="twitter-share-button" data-text="${_insight.content} &{'onbeansight'}" data-count="horizontal">Tweet</a><script type="text/javascript" src="http://platform.twitter.com/widgets.js"></script>
           </span>
           
           <span id="shareOnFacebook">
	       <script src="http://connect.facebook.net/en_US/all.js#xfbml=1"></script><fb:like show_faces="false" width="250"></fb:like>
           </span>
	</div>



</div>