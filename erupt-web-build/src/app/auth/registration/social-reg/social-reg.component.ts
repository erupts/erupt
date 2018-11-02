import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-social-reg',
  templateUrl: './social-reg.component.html',
  styleUrls: ['./social-reg.component.scss']
})
export class SocialRegComponent implements OnInit {

  constructor() { }

  ngOnInit() {
    document.querySelector('body').setAttribute('themebg-pattern', 'theme1');
  }

}
