*{Generates a <select> element populated with the categories of the website}*
*{param none: should a "None" option be available }*

<select name="categoryId" id="categoryId">
   #{if _none==true }
        <option value="" selected >None</option>
   #{/if}
   #{list items:controllers.Application.getCategories(), as:'category'}
       <option value="${category.id}">${category.label}</option>
   #{/list}
</select>