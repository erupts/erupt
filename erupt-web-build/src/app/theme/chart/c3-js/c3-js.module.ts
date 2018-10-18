import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { C3JsComponent } from './c3-js.component';
import {C3JsRoutingModule} from './c3-js-routing.module';
import {SharedModule} from '../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    C3JsRoutingModule,
    SharedModule
  ],
  declarations: [C3JsComponent]
})
export class C3JsModule { }
