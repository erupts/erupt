import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderFooterLoginComponent } from './header-footer-login.component';
import {HeaderFooterLoginRoutingModule} from './header-footer-login-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    HeaderFooterLoginRoutingModule,
    SharedModule
  ],
  declarations: [HeaderFooterLoginComponent]
})
export class HeaderFooterLoginModule { }
