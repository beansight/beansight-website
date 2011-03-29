*{ Display the Sharing options }*
*{ @param insight : the insight to share }*

<div class="sharezone">
	
	<div class="sharebox" id="sharezone">
	       #{isConnected}
	       <span id="shareOnBeansight" class="shareItem">
	       <a href="#" id="shareOnBeansight-link" class="uiButton shareButton">&{'shareInsight.shareonbeansight'}</a>
           </span>
           <div id="shareOnBeansight-box" style="display:none; margin-top: 15px;" class="pinnedBox">
	           <form id="shareOnBeansightForm">
	               <p>&{'shareInsight.searchBeansightUser'}</p>
	               <input type="text" name="userName" id="userToShareTo" />
	               <input type="hidden" name="insightUniqueId" value="${_insight.uniqueId}" />
	               <input type="submit" value="&{'shareInsight.sharebutton'}"/>
	           </form>
	           <span id="shareConfirmation"></span>
           </div>
           #{/isConnected}
           
           <span id="shareOnTwitter" class="shareItem">
	       <a href="http://twitter.com/share" class="twitter-share-button" data-text="${_insight.content} &{'onbeansight'}" data-count="horizontal">Tweet</a><script type="text/javascript" src="http://platform.twitter.com/widgets.js"></script>
           </span>
           
           <span id="shareOnFacebook" class="shareItem">
	       <script src="http://connect.facebook.net/en_US/all.js#xfbml=1"></script><fb:like layout="button_count" show_faces="false" width="50"></fb:like>
           </span>
           <hr class="clear"/>
	</div>



</div>