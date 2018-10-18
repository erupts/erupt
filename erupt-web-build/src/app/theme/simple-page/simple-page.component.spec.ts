import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SimplePageComponent } from './simple-page.component';

describe('SimplePageComponent', () => {
  let component: SimplePageComponent;
  let fixture: ComponentFixture<SimplePageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SimplePageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SimplePageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
