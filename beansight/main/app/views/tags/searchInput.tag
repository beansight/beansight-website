*{ The input box to perform a search }*

<div class="formsearch">
#{form @Application.search()}
    <div class="searchinput">
        <label for="query" class="cuf-grb">Search the future</label>
        <div class="inputstyle">
            <input type="text" name="query" id="txtseach" value="" class="inputtxt" placeholder="Search"/>
            <input type="submit" id="searchsubmit" class="inputsubmit"/>
        </div>
    </div>
#{/form}
</div>
