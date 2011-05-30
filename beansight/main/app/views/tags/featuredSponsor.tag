#{if _featuredSponsor}
<div class="featuredSponsor">
	<p>&{'insightLine.sponsoredby'}<a href="@{Application.showUser(_featuredSponsor.sponsor.userName)}">${_featuredSponsor.sponsor.userName}</a>:</p>
	<ul>
	#{list items:_featuredSponsor.insights, as:'featureSponsorInsight'}
    	<li>#{insight insight:featureSponsorInsight, characters:42 /}</li>
	#{/list}
	</ul>
</div>
#{/if}
