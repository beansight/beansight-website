*{Generates a <select> element populated with the categories of the website}*
*{param none: should a "None" option be available }*
*{param all: should a "All" option be available }*
<div class="item-select">
<select name="categoryId" id="c_category">
   #{if _none==true }
        <option value="" selected >None</option>
   #{/if}
   #{if _all==true }
        <option value="all" selected >All</option>
   #{/if}
   #{list items:controllers.Application.getCategories(), as:'category'}
       <option value="${category.id}">${category.label}</option>
   #{/list}
</select>
</div>
