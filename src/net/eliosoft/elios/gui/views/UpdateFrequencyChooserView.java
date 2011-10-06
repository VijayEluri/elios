package net.eliosoft.elios.gui.views;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;

import net.eliosoft.elios.gui.models.UpdateModel;
import net.eliosoft.elios.gui.models.UpdateModel.Frequency;

public class UpdateFrequencyChooserView implements ViewInterface {

	private final UpdateModel model;

	public UpdateFrequencyChooserView(UpdateModel updateModel) {
		this.model = updateModel;
	}
	
	@Override
	public JComponent getViewComponent() {
		final JComboBox freqCbx = new JComboBox(Frequency.values());

		freqCbx.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.saveUpdateFrequency((Frequency)freqCbx.getSelectedItem());
			}
		});
		
		freqCbx.setRenderer(new DefaultListCellRenderer() {

			/** serial id. **/
			private static final long serialVersionUID = 1772321132298800996L;

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index,
						isSelected, cellHasFocus);
				Frequency freq = (Frequency) value;
				setText(Messages.getString("update.freqency." + freq.getKey()));
				return this;
			}
		});

		freqCbx.setSelectedIndex(model.getFrequency().ordinal());
		return freqCbx;
	}

	@Override
	public String getLocalizedTitle() {
		return Messages.getString("updatefreqchooserview.title");
	}
}