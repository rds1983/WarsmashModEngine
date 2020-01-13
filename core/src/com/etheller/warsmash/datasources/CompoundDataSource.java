package com.etheller.warsmash.datasources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CompoundDataSource implements DataSource {
	private final List<DataSource> mpqList = new ArrayList<>();

	public CompoundDataSource(final List<DataSourceDescriptor> dataSourceDescriptors) {
		if (dataSourceDescriptors != null) {
			for (final DataSourceDescriptor descriptor : dataSourceDescriptors) {
				this.mpqList.add(descriptor.createDataSource());
			}
		}
	}

	Map<String, File> cache = new HashMap<>();

	@Override
	public File getFile(final String filepath) {
		if (this.cache.containsKey(filepath)) {
			return this.cache.get(filepath);
		}
		try {
			for (int i = this.mpqList.size() - 1; i >= 0; i--) {
				final DataSource mpq = this.mpqList.get(i);
				final File tempProduct = mpq.getFile(filepath);
				if (tempProduct != null) {
					this.cache.put(filepath, tempProduct);
					return tempProduct;
				}
			}
		}
		catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (filepath.toLowerCase(Locale.US).endsWith(".blp")) {
			return getFile(filepath.substring(0, filepath.lastIndexOf(".")) + ".dds");
		}
		if (filepath.toLowerCase(Locale.US).endsWith(".tif")) {
			return getFile(filepath.substring(0, filepath.lastIndexOf(".")) + ".dds");
		}
		return null;
	}

	@Override
	public InputStream getResourceAsStream(final String filepath) {
		try {
			for (int i = this.mpqList.size() - 1; i >= 0; i--) {
				final DataSource mpq = this.mpqList.get(i);
				final InputStream resourceAsStream = mpq.getResourceAsStream(filepath);
				if (resourceAsStream != null) {
					return resourceAsStream;
				}
			}
		}
		catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (filepath.toLowerCase(Locale.US).endsWith(".blp")) {
			return getResourceAsStream(filepath.substring(0, filepath.lastIndexOf(".")) + ".dds");
		}
		if (filepath.toLowerCase(Locale.US).endsWith(".tif")) {
			return getResourceAsStream(filepath.substring(0, filepath.lastIndexOf(".")) + ".dds");
		}
		return null;
	}

	@Override
	public boolean has(final String filepath) {
		if (this.cache.containsKey(filepath)) {
			return true;
		}
		for (int i = this.mpqList.size() - 1; i >= 0; i--) {
			final DataSource mpq = this.mpqList.get(i);
			if (mpq.has(filepath)) {
				return true;
			}
		}
		if (filepath.toLowerCase(Locale.US).endsWith(".blp")) {
			return has(filepath.substring(0, filepath.lastIndexOf(".")) + ".dds");
		}
		if (filepath.toLowerCase(Locale.US).endsWith(".tif")) {
			return has(filepath.substring(0, filepath.lastIndexOf(".")) + ".dds");
		}
		return false;
	}

	public void refresh(final List<DataSourceDescriptor> dataSourceDescriptors) {
		for (final DataSource dataSource : this.mpqList) {
			try {
				dataSource.close();
			}
			catch (final NullPointerException e) {
				e.printStackTrace();
			}
			catch (final IOException e) {
				e.printStackTrace();
			}
		}
		this.cache.clear();
		this.mpqList.clear();
		if (dataSourceDescriptors != null) {
			for (final DataSourceDescriptor descriptor : dataSourceDescriptors) {
				this.mpqList.add(descriptor.createDataSource());
			}
		}
	}

	public interface LoadedMPQ {
		void unload();

		boolean hasListfile();

		boolean has(String path);
	}

	public Set<String> getMergedListfile() {
		final Set<String> listfile = new HashSet<>();
		for (final DataSource mpqGuy : this.mpqList) {
			final Collection<String> dataSourceListfile = mpqGuy.getListfile();
			if (dataSourceListfile != null) {
				for (final String element : dataSourceListfile) {
					listfile.add(element);
				}
			}
		}
		return listfile;
	}

	@Override
	public Collection<String> getListfile() {
		return getMergedListfile();
	}

	@Override
	public void close() throws IOException {
		for (final DataSource mpqGuy : this.mpqList) {
			mpqGuy.close();
		}
	}
}
