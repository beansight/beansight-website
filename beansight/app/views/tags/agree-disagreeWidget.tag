*{ Display the info for a given insight  }*
*{ @param insigth: the insight  }*

<span onClick="agree(${_insight.id})" ><a href="" onClick="return false" class="icon agree"></a></span> (<span class="agreeCount">${_insight.agreeCount}</span>)
<span onClick="disagree(${_insight.id})" ><a href="" onClick="return false" class="icon disagree"></a></span> (<span class="disagreeCount">${_insight.disagreeCount}</span>)