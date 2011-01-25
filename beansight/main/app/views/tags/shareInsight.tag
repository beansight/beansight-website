*{ Display the Sharing options }*
*{ @param insight : the insight to share }*

<div class="sharezone">
    <p>&{'sharethisinsight'}</p>

*{ For the moment, do not show "Share Buttons"
    <div class="onTwitter">
        <a href="http://twitter.com/share" class="twitter-share-button" data-text="${_insight.content} &{'onbeansight'}" data-count="horizontal">Tweet</a><script type="text/javascript" src="http://platform.twitter.com/widgets.js"></script>
    </div>
    <div class="onFacebook">
        <script src="http://connect.facebook.net/en_US/all.js#xfbml=1"></script><fb:like show_faces="false" width="450"></fb:like>
    </div>
}*

    <div class="onBeansight input-submit">
       <button id="shareOnBeansight"><span class="backbutton"></span><span class="txtbutton cuf-newaccount">&{'shareonbeansight'}</span></button>
       <form id="shareOnBeansightForm" style="display:none;">
           <input type="text" name="userName" id="userToShareTo" />
           <input type="hidden" name="insightUniqueId" value="${_insight.uniqueId}" />
       </form>
       <span id="shareConfirmation"></span>
    </div>
</div>