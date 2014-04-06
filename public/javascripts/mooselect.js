/*
Script: 		comboBoo.js
License:		MIT-style license.
Credits:		Julien Colorz - www.colorz.fr | based on Bruno Torrinha - www.torrinha.com
				Mootools 1.2 compatible
				Support long list by adding overflow scroll.
				Mootools framework - mootools.net.
*/

var comboBoo = new Class({
	options: {
		className: 'comboBoo',
		maxHeight:130,
		selectBgColorOver:'',
		selectBgColorOut:''
	},
	

	initialize: function(el, options){
		
		/*this.setOptions(options);*/
		this.oldCombo = $(el);
		this.bOpen = false;
		
		var pos = el.getCoordinates();
		
		this.oldCombo.setStyles({'position':'absolute','visibility':'hidden','left':(this.oldCombo.getWidth()*-1)+'px'});
		/*console.log(pos);*/
		this.comboLink = new Element('a', {'class': this.options.className + '-label', 'id': el.name})
			//.setStyles({top: pos.top+'px', left: pos.left+'px',position:'absolute','background-color':this.options.selectBgColorOut})
			.setStyles({ width: pos.width+'px','background-color':this.options.selectBgColorOut})
			//.inject(document.body,'inside').set('html',el.options[el.options.selectedIndex].text);
			.inject(el,'after').set('html',el.options[el.options.selectedIndex].text);
		
		this.comboList = new Element('ul', {'class': this.options.className + '-list', 'id': 'choices-' + el.name}).setStyles({margin:0,padding:0});
			
				
			
		this.divList = new Element('div', {'class':'div-list', 'id': 'divchoices-' + el.name})
						.setStyles({ top: pos.top+pos.height+'px', left: pos.left+'px', width: pos.width + 33+'px',position:'absolute','z-index':10000})
						.inject($(document.body),'bottom')
						.adopt(this.comboList);
		
		this.fx={	comboLink: new Fx.Morph(this.comboLink, {duration: '300', transition: Fx.Transitions.Sine.easeOut,wait:false}),
					comboList: new Fx.Morph(this.divList, {duration: '500', transition: Fx.Transitions.Sine.easeOut,wait:false})}
	
			
		this.build(el);

	},

	build: function(el){
		
		
		for(i = 0; i < el.length; i++) {
			
			var el2 = new Element('li', {'id': i}).set('html',el.options[i].text);
			this.addChoiceEvents(el2).inject(this.comboList,'inside');
			
			
			/*if(el.options[i].getProperty('selected'))
			this.choiceSelect(el.options[i]);*/
			
		

		}
		this.addComboLinkEvents(this.comboLink);
		/* combo scroll*/
		if(this.comboList.getHeight()>this.options.maxHeight)
		this.divList.setStyles({height:this.options.maxHeight+'px'});
		this.fx.comboList.set({'opacity':0});
	},

	click: function(el) {

		if (this.bOpen) {
			this.bOpen = false;
		this.fx.comboList.start({'opacity':0});
		}else{
			this.bOpen = true;
		this.fx.comboList.set({'top':this.comboLink.getTop()+this.comboLink.getHeight()});	
		this.fx.comboList.start({'opacity':1});

		}		
		
	},

	comboOver: function() {
		
		if (!this.bOpen) this.fx.comboLink.start({'background-color':this.options.selectBgColorOver});
	},

	comboOut: function(el) {
	
		if (!this.bOpen) this.fx.comboLink.start({'background-color':this.options.selectBgColorOut});
	},

	choiceOver: function(el) {
		if (this.selected) this.selected.removeClass('choice-selected');
		this.selected = el.addClass('choice-selected');
	
	},

	choiceSelect: function(el) {
	
			this.bOpen = false;
			
		
		this.fx.comboList.start({'opacity':0,'background-color':eval(this.options.selectBgColorOut)});
		this.comboLink.set('html',el.get('text'));
		this.oldCombo.selectedIndex = el.id;
	},

	addComboLinkEvents: function(el) {
		
		return el.addEvents({
			click: this.click.bind(this, [el]),
			mouseover: this.comboOver.bind(this, [el]), 
			mouseleave: this.comboOut.bind(this, [el])
		});

		
	},

	addChoiceEvents: function(el) {
		
		return el.addEvents({
			mouseover: this.choiceOver.bind(this, [el]),
			mousedown: this.choiceSelect.bind(this, [el])
		});

		
		
	}
});

comboBoo.implement(new Events, new Options);