import { Component, OnInit } from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {CustomValidators} from 'ng2-validation';

@Component({
  selector: 'app-form-validation',
  templateUrl: './form-validation.component.html',
  styleUrls: [
    './form-validation.component.scss',
    '../../../../assets/icon/icofont/css/icofont.scss'
  ]
})
export class FormValidationComponent implements OnInit {
  myForm: FormGroup;
  mynumberForm: FormGroup;
  mytooltipForm: FormGroup;
  checkdropForm: FormGroup;
  submitted: boolean;

  constructor() {

    const name = new FormControl('', Validators.required);
    const password = new FormControl('', Validators.required);
    const gender = new FormControl('', Validators.required);
    const email = new FormControl('', [Validators.required, Validators.email]);

    const rpassword = new FormControl('', [Validators.required, CustomValidators.equalTo(password)]);
    this.myForm = new FormGroup({
      name: name,
      email: email,
      password: password,
      rpassword: rpassword,
      gender: gender
    });
    /*Basic validation end*/

    /*number Validation start*/
    const integer = new FormControl('', [Validators.required, CustomValidators.digits]);
    const numeric = new FormControl('', [Validators.required, CustomValidators.number]);
    const greater = new FormControl('', [Validators.required, CustomValidators.gt(50)]);
    const less = new FormControl('', [Validators.required, CustomValidators.lt(50)]);

    this.mynumberForm = new FormGroup({
      integer: integer,
      numeric: numeric,
      greater: greater,
      less: less
    });
    /*number validation end*/

    /*Tooltip Validation Start*/
    const usernameP = new FormControl('', [Validators.required]);
    const EmailP = new FormControl('', [Validators.required, Validators.email]);
    this.mytooltipForm = new FormGroup({
      usernameP: usernameP,
      EmailP: EmailP,
    });
    /*Tooltip Validation End*/

    /* component form */
    const area = new FormControl('', [Validators.required]);
    const job = new FormControl('', [Validators.required]);
    this.checkdropForm = new FormGroup({
      area: area,
      job: job,
    });
    /* end component form */
  }

  ngOnInit() {
  }

  onSubmit() {
    this.submitted = true;
    console.log(this.myForm);
  }

}
