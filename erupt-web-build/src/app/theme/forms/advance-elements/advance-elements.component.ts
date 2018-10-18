import { Component, OnInit } from '@angular/core';
import {FormControl} from '@angular/forms';

@Component({
  selector: 'app-advance-elements',
  templateUrl: './advance-elements.component.html',
  styleUrls: [
    './advance-elements.component.scss',
    '../../../../assets/icon/icofont/css/icofont.scss'
  ]
})
export class AdvanceElementsComponent implements OnInit {
  switchDisable = true;
  items = ['Amsterdam', 'Washington', 'Sydney'];
  itemsAsObjects = [{id: 0, name: 'Amsterdam', readonly: true}, {id: 1, name: 'Washington'}];

  public errorMessages = {
    'startsWithAt@': 'Your items need to start with \'@\'',
    'endsWith$': 'Your items need to end with \'$\''
  };

  public validators = [this.startsWithAt, this.endsWith$];

  constructor() { }

  ngOnInit() {
  }

  disableSwitch() {
    this.switchDisable = true;
  }

  enableSwitch() {
    this.switchDisable = false;
  }

  private startsWithAt(control: FormControl) {
    if (control.value.charAt(0) !== '@') {
      return {
        'startsWithAt@': true
      };
    }

    return null;
  }

  private endsWith$(control: FormControl) {
    if (control.value.charAt(control.value.length - 1) !== '$') {
      return {
        'endsWith$': true
      };
    }

    return null;
  }

}
