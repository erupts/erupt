import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EditTypeComponent } from './edit-type.component';

describe('EditTypeComponent', () => {
  let component: EditTypeComponent;
  let fixture: ComponentFixture<EditTypeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EditTypeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditTypeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
