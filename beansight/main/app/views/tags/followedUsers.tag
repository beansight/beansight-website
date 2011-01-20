<div class="list-avatars">
#{list items:_followedUsers , as:'user'}
    <li>#{userInline user:user /}</li>
#{/list}
</div>