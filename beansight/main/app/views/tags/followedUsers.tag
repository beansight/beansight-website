<ul>
#{list items:_followedUsers , as:'user'}
    <li>#{userInline user:user /}</li>
#{/list}
</ul>