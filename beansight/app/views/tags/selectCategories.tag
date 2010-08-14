*{Generates a <select> element populated with the categories of the website}*

<select name="categoryId" id="categoryId">
   #{list items:controllers.Application.getCategories(), as:'category'}
       <option value="${category.id}">${category.label}</option>
   #{/list}
</select>