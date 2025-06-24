function changeColor() {
	var title1 = document.getElementById('title1');
	var title2 = document.getElementById('title2');
	if (title1.className == 'color1' || title2.className == 'color1') {
		title1.className = 'color2';
		title2.className = 'color2';
	} else {
		title1.className = 'color1';
		title2.className = 'color1';
	}
}