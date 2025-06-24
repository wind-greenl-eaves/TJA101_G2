function changeColor() {
	var title1 = document.getElementById('title1');
	var title2 = document.getElementById('title2');
	var title3 = document.getElementById('title3');
	var title4 = document.getElementById('title4');
	if (title1.className == 'color1' || title2.className == 'color1' || title3.className == 'color1' || title4.className == 'color1') {
		title1.className = 'color2';
		title2.className = 'color2';
		title3.className = 'color2';
		title4.className = 'color2';
	} else {
		title1.className = 'color1';
		title2.className = 'color1';
		title3.className = 'color1';
		title4.className = 'color1';
	}
}