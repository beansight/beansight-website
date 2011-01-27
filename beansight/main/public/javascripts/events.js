$(document).ready(function() {

    // Supprime les class nojavascript qui ne servent que pour ceux qui n'ont pas javascript
    $('.nojavascript').removeClass('nojavascript');

    // zone d'identification
    if($('#boxlogin') && $('#btnlogin') && $('#zonelogin')){
        var heightlogbox = $('#boxlogin').height();
        $('#boxlogin').animate({
            height: '0'
            }, 10
        );
        $('#btnlogin').addClass('open');
        $('#btnlogin').click(function(e) {
            if($('#btnlogin').hasClass('open')){
                $('#btnlogin').removeClass('open');
                $('#boxlogin').animate({
                    height: heightlogbox+10
                }, 500
                );
            }else{
                $('#btnlogin').addClass('open');
                $('#boxlogin').animate({
                    height: '0'
                    }, 500
                );
            }
            return false;
        });
        $('#zonelogin').live('mouseleave', function() {
            $.data(this, 'timer', setTimeout(function() {
                $('#btnlogin').addClass('open');
                $('#boxlogin').animate({
                    height: '0'
                    }, 500
                );
            }, 800));

        }).live('mouseenter', function() {
          clearTimeout($.data(this, 'timer'));
        });

        // zone identification fade 
        if($('#boxlog-bean') && $('#log-bean')){
            $('.item-loginbox').fadeOut(200);
            $('#boxlog-bean').fadeIn(200);
            $('.listlogin li a').click(function(e) {
                $('.listlogin li a').removeClass('current');
                jQuery(this).addClass('current');
                $('.item-loginbox').fadeOut(200);
                $('#box'+jQuery(this).attr('id')).delay(400).fadeIn(200);
                $('#boxlogin .list-logins').animate({
                    height: $('#box'+jQuery(this).attr('id')).height()
                    }, 500
                );
                return false;
            });
        }

    }


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


    //Création de compte formulaire
    //drapeau
    $('.item-radio label').click(function(e) {
        $('.item-radio label').removeClass('current');
        $(this).addClass('current');
    });
    //select custom
    $(".item-select select").selectbox();
    //tags
    function clicktags(){
        $(".listtags a").each(function(i){
            $(this).click(function(e) {
                $(this).remove();
                registertags();
                return false;
            });
        });
    }
    function validetag(){
        if($('#taginput').attr('value')!=',' && $('#taginput').attr('value')!=';' && $('#taginput').attr('value').replace(/( |,|;)/ig, '').length>0){
            $('#newtag').attr('id', '');
            $('#taginput').attr('value', '');
        }
    }
    function registertags(){
        var compteur = 0;
        $('#tagresult').attr('value', '');
        $(".listtags a span").each(function(i){
            if($(this).html().replace(/( |,|;)/ig, '').length>0 && $(this).html()!=';' && $(this).html()!=','){
                if(compteur>0)
                    $('#tagresult').attr('value', $('#tagresult').attr('value')+', ');
                $('#tagresult').attr('value', $('#tagresult').attr('value')+$(this).html());;
                compteur++;
            }
        });
    }
    if($('#taginput')){
        clicktags();
        registertags();
        $('#taginput').keyup(function(event) {
            if($('#newtag').length){
                if($('#taginput').attr('value').indexOf(',')!=-1 || $('#taginput').attr('value').indexOf(';')!=-1){
                    validetag();
                    registertags();
                }else if($('#taginput').attr('value')==''){
                    $('#newtag').remove();
                }else{
                    $('#newtag').html('<span>'+$('#taginput').attr('value').replace(/(;|,)/ig, '')+'</span>');
                }
            }else if($('#taginput').attr('value')!=''){
                $('<a href="#" id="newtag">'+$('#taginput').attr('value').replace(/(;|,)/ig, '')+'</a>').appendTo(".listtags");
                clicktags();
                //$('.listtags').add('a').attr('href', '').attr('id', 'newtag');
            }
        });
        $('#taginput').keypress(function(event) {
            if(event.keyCode=='13'){
                validetag();
                registertags();
                return false;
            }
        });
    }
    

    
    //Fin de création de compte formulaire




    /* ENVOI DE MAIL */
    if($('#link-sendmail')){
        var heightmailbox = $('#boxlink-sendmail').height();
        $('#boxlink-sendmail').animate({
            height: '0'
            }, 10
        );
        $('#link-sendmail').removeClass('open');
        $('#link-sendmail').click(function(e) {
            if(!$('#link-sendmail').hasClass('open')){
                $('#link-sendmail').addClass('open');
                $('#boxlink-sendmail').animate({
                    height: heightmailbox
                }, 500
                );
            }else{
                $('#link-sendmail').removeClass('open');
                $('#boxlink-sendmail').animate({
                    height: '0'
                    }, 500
                );
            }
            return false;
        });
    }





});


