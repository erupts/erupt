import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SocialHeaderFooterLoginComponent } from './social-header-footer-login.component';
import {SocialHeaderFooterLoginRoutingModule} from './social-header-footer-login-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    SocialHeaderFooterLoginRoutingModule,
    SharedModule
  ],
  declarations: [SocialHeaderFooterLoginComponent]
})
export class SocialHeaderFooterLoginModule { }
