<div class="list-avatars">
#{list items:_followedUsers , as:'user'}
    <li>#{userMiniAvatarName user:user /}</li>
#{/list}
</div>