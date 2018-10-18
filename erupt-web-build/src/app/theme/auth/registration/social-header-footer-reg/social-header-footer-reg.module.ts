import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SocialHeaderFooterRegComponent } from './social-header-footer-reg.component';
import {SocialHeaderFooterRegRoutingModule} from './social-header-footer-reg-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    SocialHeaderFooterRegRoutingModule,
    SharedModule
  ],
  declarations: [SocialHeaderFooterRegComponent]
})
export class SocialHeaderFooterRegModule { }
