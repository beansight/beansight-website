*{Generates a list with the categories of the website, clicking on a category refines the current insights }*
*{@param all: should a "All" option be available }*
*{@param action: "insights" or "search", the name of the action to perform when clicking on a facette }*

<ul>
   #{if _all==true }
        <a href=@{Application.insights()}>All</a>
   #{/if}
   #{list items:controllers.Application.getCategories(), as:'category'}
       <a href="@{Application.insights(category.id)}">${category.label}</a>
   #{/list}
</ul>