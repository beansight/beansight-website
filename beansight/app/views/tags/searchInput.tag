*{ The input box to perform a search }*

<p>Search the future:</p>
<p>
    #{form @Application.search()}
        <input type="text" name="query" id="query" />
        <input type="submit" value="Search" />
    #{/form}
</p>
