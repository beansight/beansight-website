*{ generates the right number of stars }*
*{ @param score : score between 0 and 1 }*
%{
long fullStarsNumber  = Math.floor( _score * 5 );

long halfStar = 0;
if( _score * 5 - fullStarsNumber > 0.5 ) {
    halfStar = 1;
}

long emptyStarsNumber = 5 - fullStarsNumber - halfStar;
}%

<div class="vote">
%{ for(int i = 0; i < fullStarsNumber; i++) { }%
    <span class="star fullstar">star</span>
%{ } }%

#{if halfStar > 0}
<span class="star demistar">half-star</span>
#{/if}

%{ for(int i = 0; i < emptyStarsNumber; i++) { }%
    <span class="star emptystar"></span>
%{ } }%
</div>
