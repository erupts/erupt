import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdvanceElementsComponent } from './advance-elements.component';
import {AdvanceElementsRoutingModule} from './advance-elements-routing.module';
import {SharedModule} from '../../../shared/shared.module';
import {TagInputModule} from 'ngx-chips';
import {FormsModule} from '@angular/forms';
import {UiSwitchModule} from "ng2-ui-switch/dist";

@NgModule({
  imports: [
    CommonModule,
    AdvanceElementsRoutingModule,
    SharedModule,
    UiSwitchModule,
    TagInputModule,
    FormsModule
  ],
  declarations: [AdvanceElementsComponent]
})
export class AdvanceElementsModule { }
