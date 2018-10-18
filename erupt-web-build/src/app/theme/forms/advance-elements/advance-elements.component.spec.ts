import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AdvanceElementsComponent } from './advance-elements.component';

describe('AdvanceElementsComponent', () => {
  let component: AdvanceElementsComponent;
  let fixture: ComponentFixture<AdvanceElementsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AdvanceElementsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AdvanceElementsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
