#{if _featuredSponsor}
<div class="featuredSponsor">
	<h3>&{'insightLine.sponsoredby'}<a href="@{Application.showUser(_featuredSponsor.sponsor.userName)}">${_featuredSponsor.sponsor.userName}</a>:</h3>
	<ul>
	#{list items:_featuredSponsor.insights, as:'featureSponsorInsight'}
    	<li>#{insight insight:featureSponsorInsight, characters:38 /}</li>
	#{/list}
	</ul>
</div>
#{/if}
