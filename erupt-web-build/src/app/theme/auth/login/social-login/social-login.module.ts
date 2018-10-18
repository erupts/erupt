import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SocialLoginComponent } from './social-login.component';
import {SocialLoginRoutingModule} from './social-login-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    SocialLoginRoutingModule,
    SharedModule
  ],
  declarations: [SocialLoginComponent]
})
export class SocialLoginModule { }
