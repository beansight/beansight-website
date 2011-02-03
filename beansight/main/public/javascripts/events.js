$(document).ready(function() {

    // Supprime les class nojavascript qui ne servent que pour ceux qui n'ont pas javascript
    $('.nojavascript').removeClass('nojavascript');


    // select de tri des insights
    $(".selectme").each(function(i){
        var element = $(this);
        var idelement = element.children('a').attr('id');
        var idbox = '#ul'+idelement;
        idelement = '#'+idelement;
        var heightlogbox = $(idbox).height();
        $(idbox).animate({
            height: '0'
            }, 500
        );
        $(idbox).addClass('open');
        $(idelement).click(function(e) {
            if($(idbox).hasClass('open')){
                $(idbox).removeClass('open');
                $(idbox).animate({
                    height: heightlogbox
                }, 500
                );
            }else{
                $(idbox).addClass('open');
                $(idbox).animate({
                    height: '0'
                    }, 500
                );
            }
            return false;
        });
        $(idbox).live('mouseleave', function() {
            $.data(this, 'timer', setTimeout(function() {
                $(idbox).addClass('open');
                $(idbox).animate({
                    height: '0'
                    }, 500
                );
            }, 800));

        }).live('mouseenter', function() {
          clearTimeout($.data(this, 'timer'));
        });
    });


    //drapeau
    $('.item-radio label').click(function(e) {
        $('.item-radio label').removeClass('current');
        $(this).addClass('current');
    });
    
    //select custom
    $(".item-select select").selectbox();
});


