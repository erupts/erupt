import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderFooterRegComponent } from './header-footer-reg.component';
import {HeaderFooterRegRoutingModule} from './header-footer-reg-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    HeaderFooterRegRoutingModule,
    SharedModule
  ],
  declarations: [HeaderFooterRegComponent]
})
export class HeaderFooterRegModule { }
